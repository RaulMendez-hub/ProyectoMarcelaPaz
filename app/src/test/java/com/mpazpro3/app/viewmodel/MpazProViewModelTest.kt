package com.mpazpro3.app.viewmodel

import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias de las reglas de negocio de [MpazProViewModel].
 *
 * Se ejecutan en la JVM (sin emulador) con `./gradlew :app:testDebugUnitTest`.
 * Cada prueba crea su propio ViewModel: los StateFlow parten desde MockData y las
 * actualizaciones no modifican los datos compartidos, así que las pruebas son independientes.
 */
class MpazProViewModelTest {

    private lateinit var viewModel: MpazProViewModel

    private val curso = "3° básico A"
    private val estudiante = "EST-001"

    /** C2 está APROBADO en MockData y ya tiene asignación (2 intentos) y evaluación final. */
    private val contenidoAprobado = "C2"

    /** C1 está PENDIENTE de autorización y no tiene evaluación final registrada. */
    private val contenidoPendiente = "C1"

    /** C4 está RECHAZADO por UTP. */
    private val contenidoRechazado = "C4"

    @Before
    fun setUp() {
        viewModel = MpazProViewModel()
    }

    // ---------------------------------------------------------------------
    // asignarAlCurso
    // ---------------------------------------------------------------------

    @Test
    fun `asignarAlCurso falla si el curso esta vacio`() = runTest {
        val asignacionesAntes = viewModel.asignacionesDelCurso(curso)

        val resultado = viewModel.asignarAlCurso(curso = "   ", contenidoIds = listOf(contenidoAprobado), maxIntentos = 2)

        assertFalse("Un curso en blanco no debe permitir asignar contenidos", resultado)
        assertEquals(MensajeRegla.CURSO_VACIO, viewModel.mensajeRegla.value)
        assertEquals("No se debe crear ninguna asignación", asignacionesAntes, viewModel.asignacionesDelCurso(curso))
    }

    @Test
    fun `asignarAlCurso falla si el maximo de intentos es menor a 1`() = runTest {
        listOf(0, -1, -10).forEach { invalidos ->
            viewModel = MpazProViewModel()
            val asignacionesAntes = viewModel.asignacionesDelCurso(curso)

            val resultado = viewModel.asignarAlCurso(curso, listOf(contenidoAprobado), invalidos)

            assertFalse("maxIntentos=$invalidos debe ser rechazado", resultado)
            assertEquals(MensajeRegla.INTENTOS_INVALIDOS, viewModel.mensajeRegla.value)
            assertEquals(asignacionesAntes, viewModel.asignacionesDelCurso(curso))
        }
    }

    @Test
    fun `asignarAlCurso falla si algun contenido no esta autorizado`() = runTest {
        // PENDIENTE, RECHAZADO e inexistente deben rechazarse por separado.
        listOf(contenidoPendiente, contenidoRechazado, "NO-EXISTE").forEach { noAutorizado ->
            viewModel = MpazProViewModel()
            val asignacionesAntes = viewModel.asignacionesDelCurso(curso)

            val resultado = viewModel.asignarAlCurso(curso, listOf(contenidoAprobado, noAutorizado), 2)

            assertFalse("El contenido $noAutorizado no está autorizado", resultado)
            assertEquals(MensajeRegla.CONTENIDOS_NO_AUTORIZADOS, viewModel.mensajeRegla.value)
            assertEquals("No se asigna ninguno si uno falla", asignacionesAntes, viewModel.asignacionesDelCurso(curso))
        }
    }

    @Test
    fun `asignarAlCurso registra la asignacion y actualiza intentos sin duplicar`() = runTest {
        val resultado = viewModel.asignarAlCurso(curso, listOf(contenidoAprobado), 3)

        assertTrue(resultado)
        assertEquals(MensajeRegla.ASIGNACION_REGISTRADA, viewModel.mensajeRegla.value)

        val asignaciones = viewModel.asignacionesDelCurso(curso).filter { it.contenidoId == contenidoAprobado }
        assertEquals("C2 ya estaba asignado: no debe duplicarse", 1, asignaciones.size)
        assertEquals("Debe actualizarse el máximo de intentos", 3, asignaciones.single().maxIntentosEvaluacion)
    }

    @Test
    fun `asignarAlCurso no acepta un contenido recien rechazado`() = runTest {
        viewModel.rechazarContenido(contenidoAprobado, "Falta el recurso de referencia")
        assertEquals(EstadoAprobacion.RECHAZADO, viewModel.contenidoPorId(contenidoAprobado)?.estado)

        assertFalse(viewModel.asignarAlCurso(curso, listOf(contenidoAprobado), 2))
        assertEquals(MensajeRegla.CONTENIDOS_NO_AUTORIZADOS, viewModel.mensajeRegla.value)
    }

    // ---------------------------------------------------------------------
    // enviarEvaluacion
    // ---------------------------------------------------------------------

    @Test
    fun `enviarEvaluacion falla si los intentos estan agotados`() = runTest {
        assertTrue(viewModel.asignarAlCurso(curso, listOf(contenidoAprobado), 1))
        val respuestas = respuestasCorrectas(contenidoAprobado)

        assertTrue("El primer intento debe registrarse", viewModel.enviarEvaluacion(estudiante, contenidoAprobado, respuestas))
        assertEquals(0, viewModel.intentosRestantes(estudiante, contenidoAprobado))

        val segundoIntento = viewModel.enviarEvaluacion(estudiante, contenidoAprobado, respuestas)

        assertFalse("Sin intentos restantes la evaluación no se envía", segundoIntento)
        assertEquals(MensajeRegla.INTENTOS_AGOTADOS, viewModel.mensajeRegla.value)
        assertEquals("Los intentos usados no deben aumentar", 1, seguimientoDe(contenidoAprobado).intentosEvaluacionUsados)
    }

    @Test
    fun `enviarEvaluacion falla y no consume intento si faltan respuestas`() = runTest {
        val intentosAntes = viewModel.intentosRestantes(estudiante, contenidoAprobado)
        val primeraPregunta = viewModel.evaluacionFinalDe(contenidoAprobado).first()

        val resultado = viewModel.enviarEvaluacion(
            estudiante,
            contenidoAprobado,
            mapOf(primeraPregunta.id to primeraPregunta.alternativas.indexOfFirst { it.esCorrecta })
        )

        assertFalse(resultado)
        assertEquals(MensajeRegla.RESPUESTAS_INCOMPLETAS, viewModel.mensajeRegla.value)
        assertEquals(intentosAntes, viewModel.intentosRestantes(estudiante, contenidoAprobado))
        assertEquals(0, seguimientoDe(contenidoAprobado).intentosEvaluacionUsados)
    }

    @Test
    fun `enviarEvaluacion registra el intento y finaliza el contenido`() = runTest {
        assertTrue(viewModel.enviarEvaluacion(estudiante, contenidoAprobado, respuestasCorrectas(contenidoAprobado)))

        val seguimiento = seguimientoDe(contenidoAprobado)
        assertEquals(1, seguimiento.intentosEvaluacionUsados)
        assertEquals(EstadoAvance.FINALIZADO, seguimiento.estado)
        assertEquals(1, viewModel.intentosRestantes(estudiante, contenidoAprobado))

        val preguntas = viewModel.evaluacionFinalDe(contenidoAprobado).size
        val vigentes = viewModel.respuestasDe(estudiante, contenidoAprobado)
        assertEquals("Debe quedar una respuesta vigente por pregunta", preguntas, vigentes.size)
        assertTrue("Todas las respuestas enviadas eran correctas", vigentes.all { it.esCorrecta })
    }

    @Test
    fun `enviarEvaluacion falla si el contenido no tiene preguntas`() = runTest {
        // C1 no tiene evaluación final en MockData; se autoriza para poder asignarlo.
        viewModel.aprobarContenido(contenidoPendiente, "Contenido autorizado para la prueba")
        assertTrue(viewModel.asignarAlCurso(curso, listOf(contenidoPendiente), 2))

        val resultado = viewModel.enviarEvaluacion(estudiante, contenidoPendiente, emptyMap())

        assertFalse(resultado)
        assertEquals(MensajeRegla.SIN_PREGUNTAS, viewModel.mensajeRegla.value)
    }

    @Test
    fun `intentosRestantes es cero cuando el contenido no esta asignado`() = runTest {
        // C3 existe pero no está asignado al curso del estudiante.
        assertEquals(0, viewModel.intentosRestantes(estudiante, "C3"))
        assertFalse(viewModel.enviarEvaluacion(estudiante, "C3", emptyMap()))
        assertEquals(MensajeRegla.INTENTOS_AGOTADOS, viewModel.mensajeRegla.value)
    }

    // ---------------------------------------------------------------------
    // Autorización y login (reglas de negocio restauradas)
    // ---------------------------------------------------------------------

    @Test
    fun `rechazarContenido exige un motivo`() = runTest {
        viewModel.rechazarContenido(contenidoAprobado, "   ")
        assertEquals(
            "Sin observación el contenido conserva su estado",
            EstadoAprobacion.APROBADO,
            viewModel.contenidoPorId(contenidoAprobado)?.estado
        )

        viewModel.rechazarContenido(contenidoAprobado, "Falta el recurso de referencia")
        assertEquals(EstadoAprobacion.RECHAZADO, viewModel.contenidoPorId(contenidoAprobado)?.estado)
    }

    @Test
    fun `login rechaza credenciales incorrectas y acepta las correctas`() = runTest {
        assertFalse(viewModel.login("est001", "clave-incorrecta"))
        assertNull(viewModel.usuarioActivo.value)
        assertNotNull(viewModel.errorLogin.value)

        // El usuario no distingue mayúsculas.
        assertTrue(viewModel.login("EST001", "1234"))
        assertEquals("EST-001", viewModel.usuarioActivo.value?.id)
        assertNull(viewModel.errorLogin.value)
    }

    // ---------------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------------

    /** Respuestas completas de la evaluación final eligiendo la alternativa correcta. */
    private fun respuestasCorrectas(contenidoId: String): Map<String, Int> =
        viewModel.evaluacionFinalDe(contenidoId).associate { pregunta ->
            pregunta.id to pregunta.alternativas.indexOfFirst { it.esCorrecta }
        }

    private fun seguimientoDe(contenidoId: String) =
        viewModel.seguimientos.value.single {
            it.estudianteId == estudiante && it.contenidoId == contenidoId
        }
}
