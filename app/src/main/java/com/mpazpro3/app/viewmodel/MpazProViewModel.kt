package com.mpazpro3.app.viewmodel

import androidx.lifecycle.ViewModel
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.Estudiante
import com.mpazpro3.app.model.InformeDocente
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.model.RecursoDidactico
import com.mpazpro3.app.model.RespuestaEstudiante
import com.mpazpro3.app.model.SeguimientoAvance
import com.mpazpro3.app.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update



/**
 * ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation).
 *
 * ESTO ES UN ESQUELETO: las pantallas ya llaman a todas estas funciones, pero la lógica de
 * negocio del caso está vacía a propósito y marcada con TODO(equipo), citando el módulo de
 * Requerimientos_Seccion007 al que corresponde. Mientras no se implemente, las listas se
 * muestran sin filtrar y las acciones (aprobar, asignar, responder, validar) no cambian nada.
 */
class MpazProViewModel : ViewModel() {

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /**
     * TODO(equipo): validar usuario y clave contra MockData.usuarios. Si son correctos, dejar
     * _usuarioActivo con ese usuario y devolver true (AppNavigation ya lleva a cada perfil a su
     * pantalla). Si no, dejar _errorLogin con un mensaje y devolver false.
     * Pendiente con el CITT: cómo se autentica el estudiante.
     */

    //MODIFICACION

    /**fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = "Login sin implementar — ver TODO(equipo) en MpazProViewModel.login()."
        return false
    }*/
    // VALIDACION, A TRAVES DEL MOCKDATA
    fun login(usuario: String, clave: String): Boolean {
        _errorLogin.value = null
        val usuarioEncontrado = MockData.usuarios.find {
            it.usuario.equals(usuario, ignoreCase = true) && it.clave == clave
        }

        return if (usuarioEncontrado != null) {
            _usuarioActivo.value = usuarioEncontrado
            true
        } else {
            _errorLogin.value = "Credenciales incorrectas. Verifique usuario y contraseña."
            false
        }
    }

    /**
     * SOLO PARA DESARROLLO: entra con el usuario de prueba del perfil indicado, sin validar
     * credenciales. TODO(equipo): eliminar esta función y los botones "Modo desarrollo".
     */
    fun entrarModoDesarrollo(perfil: Perfil) {
        _usuarioActivo.value = MockData.usuarios.first { it.perfil == perfil }
        _errorLogin.value = null
    }

    fun logout() {
        _usuarioActivo.value = null
        _errorLogin.value = null
        _contenidoSeleccionadoId.value = null
        _estudianteSeleccionadoId.value = null
    }

    // =====================================================================
    // Datos compartidos y selección (no es lógica de negocio)
    // =====================================================================

    private val _contenidos = MutableStateFlow(MockData.contenidos)
    val contenidos: StateFlow<List<Contenido>> = _contenidos.asStateFlow()

    private val _recursos = MutableStateFlow(MockData.recursos)
    private val _actividades = MutableStateFlow(MockData.actividades)

    private val _asignaciones = MutableStateFlow(MockData.asignaciones)
    val asignaciones: StateFlow<List<Asignacion>> = _asignaciones.asStateFlow()

    private val _estudiantes = MutableStateFlow(MockData.estudiantes)
    val estudiantes: StateFlow<List<Estudiante>> = _estudiantes.asStateFlow()

    private val _seguimientos = MutableStateFlow(MockData.seguimientos)
    val seguimientos: StateFlow<List<SeguimientoAvance>> = _seguimientos.asStateFlow()

    private val _informes = MutableStateFlow<List<InformeDocente>>(emptyList())
    val informes: StateFlow<List<InformeDocente>> = _informes.asStateFlow()

    private val _contenidoSeleccionadoId = MutableStateFlow<String?>(null)
    val contenidoSeleccionadoId: StateFlow<String?> = _contenidoSeleccionadoId.asStateFlow()



    private val _estudianteSeleccionadoId = MutableStateFlow<String?>(null)
    val estudianteSeleccionadoId: StateFlow<String?> = _estudianteSeleccionadoId.asStateFlow()

    fun seleccionarContenido(id: String?) {
        _contenidoSeleccionadoId.value = id
    }

    fun seleccionarEstudiante(id: String?) {
        _estudianteSeleccionadoId.value = id
    }

    fun contenidoPorId(id: String?): Contenido? = _contenidos.value.find { it.id == id }

    fun estudiantePorId(id: String?): Estudiante? = _estudiantes.value.find { it.id == id }

    fun recursosDe(contenidoId: String?): List<RecursoDidactico> =
        _recursos.value.filter { it.contenidoId == contenidoId }

    fun actividadPorId(id: String): Actividad? = _actividades.value.find { it.id == id }

    // =====================================================================
    // Auxiliares internos (no los usan las pantallas)
    // =====================================================================

    private fun cursoDe(estudianteId: String): String? =
        _estudiantes.value.find { it.id == estudianteId }?.curso

    private fun seguimientoDe(estudianteId: String, contenidoId: String): SeguimientoAvance? =
        _seguimientos.value.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }

    /** Crea el seguimiento si no existe y aplica el cambio de forma segura. */
    private fun actualizarSeguimiento(
        estudianteId: String,
        contenidoId: String,
        cambio: (SeguimientoAvance) -> SeguimientoAvance
    ) {
        _seguimientos.update { lista ->
            val actual = lista.find { it.estudianteId == estudianteId && it.contenidoId == contenidoId }
            if (actual == null) lista + cambio(SeguimientoAvance(estudianteId, contenidoId))
            else lista.map { if (it === actual) cambio(it) else it }
        }
    }

    private fun asignacionDe(estudianteId: String, contenidoId: String): Asignacion? {
        val curso = cursoDe(estudianteId) ?: return null
        return _asignaciones.value.find { it.curso == curso && it.contenidoId == contenidoId }
    }

    /** Contenidos APROBADOS y asignados al curso del estudiante. */
    private fun contenidosAsignados(estudianteId: String): List<Contenido> {
        val curso = cursoDe(estudianteId) ?: return emptyList()
        val ids = _asignaciones.value.filter { it.curso == curso }.map { it.contenidoId }.toSet()
        return _contenidos.value.filter { it.id in ids && it.estado == EstadoAprobacion.APROBADO }
    }

    /** Pares (correctas, total) de las respuestas vigentes del estudiante en todos sus contenidos. */
    private fun resumenRespuestas(estudianteId: String): Pair<Int, Int> {
        val respuestas = contenidosAsignados(estudianteId).flatMap { respuestasDe(estudianteId, it.id) }
        return respuestas.count { it.esCorrecta } to respuestas.size
    }


    // =====================================================================
    // Módulo "Autorización de contenidos (UTP)"
    // =====================================================================

    /**
     * TODO(equipo): filtrar por asignatura y por estado (null = todos). Hoy devuelve todo.
     */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> = _contenidos.value

    /**
     * TODO(equipo): cambiar el estado del contenido a APROBADO y guardar la observación.
     * RF: "UTP debe poder revisar y autorizar/rechazar un contenido" y "registrar el estado de
     * aprobación de cada contenido". Hoy no hace nada.
     */
    fun aprobarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    /** TODO(equipo): igual que aprobarContenido(), pero con estado RECHAZADO. Hoy no hace nada. */
    fun rechazarContenido(contenidoId: String, observacion: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Planificación docente"
    // =====================================================================

    /**
     * TODO(equipo): devolver SOLO los contenidos APROBADOS por UTP (regla de negocio: un
     * contenido no autorizado no puede asignarse). Hoy devuelve todos.
     */

    //MODIFICADO
    //fun contenidosDisponiblesParaAsignar(): List<Contenido> = _contenidos.value

    fun contenidosDisponiblesParaAsignar(): List<Contenido> {
        return _contenidos.value.filter { it.estado == EstadoAprobacion.APROBADO }
    }


    /**
     * TODO(equipo): crear las asignaciones del curso para los contenidos elegidos, con el máximo
     * de intentos de la evaluación final. RF: "seleccionar los OA disponibles", "asignar
     * contenidos y desafíos a su curso", "definir el número máximo de intentos". Validar que
     * todos los contenidos estén aprobados. Hoy no hace nada.
     */
   //fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
         //Sin implementar todavía.
    //}

    fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
        val contenidosAprobados = _contenidos.value.filter { it.id in contenidoIds && it.estado == EstadoAprobacion.APROBADO }

        if (contenidosAprobados.size != contenidoIds.size) {
            // Regla de negocio: Bloquear si existen contenidos no autorizados en la petición.
            return
        }

        val nuevasAsignaciones = contenidosAprobados.map { contenido ->
            Asignacion(
                id = "ASIG-${System.currentTimeMillis()}-${contenido.id}",
                curso = curso,
                contenidoId = contenido.id,
                maxIntentosEvaluacion = maxIntentos
            )
        }

        _asignaciones.value = (_asignaciones.value + nuevasAsignaciones).toMutableList()
    }




    /** TODO(equipo): devolver solo las asignaciones del curso indicado. Hoy devuelve todas. */
    //fun asignacionesDelCurso(curso: String): List<Asignacion> = _asignaciones.value
    fun asignacionesDelCurso(curso: String): List<Asignacion> =
        _asignaciones.value.filter { it.curso == curso }

    /**
     * TODO(equipo): calcular el avance del estudiante (0.0 a 1.0) a partir de sus
     * SeguimientoAvance. RF: "revisar los resultados de sus estudiantes". Hoy devuelve 0.
     */
    fun progresoGeneral(estudianteId: String): Float = 0f





    /**
     * TODO(equipo): exportar los resultados del curso (por ejemplo a un archivo CSV o PDF que
     * se pueda compartir). RF: "revisar, descargar y validar los resultados". Hoy no hace nada.
     */
    fun descargarResultados(curso: String) {
        // Sin implementar todavía.
    }



    // =====================================================================
    // Módulo "Informe pedagógico"
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? = _informes.value.find { it.estudianteId == estudianteId }

    /**
     * TODO(equipo): generar el informe del estudiante (rendimiento, fortalezas y aspectos a
     * reforzar) a partir de sus respuestas, en estado BORRADOR. Restricciones del caso: sin
     * diagnósticos ni consecuencias académicas automáticas. Hoy no hace nada.
     */
    fun generarInforme(estudianteId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): pasar el informe de BORRADOR a VALIDADO. RF: "el informe debe quedar
     * sujeto a validación docente antes de considerarse definitivo". Hoy no hace nada.
     */
    fun validarInforme(informeId: String) {
        // Sin implementar todavía.
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante"
    // =====================================================================

    /**
     * TODO(equipo): devolver solo los contenidos asignados al curso del estudiante. Hoy devuelve
     * todos. Regla: el estudiante no puede modificar contenidos ni ver el informe pedagógico.
     */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> = _contenidos.value

    /** TODO(equipo): avance del estudiante en el contenido (0.0 a 1.0). Hoy devuelve 0. */
    fun progreso(estudianteId: String, contenidoId: String): Float = 0f

    /**
     * TODO(equipo): guardar que el estudiante terminó de revisar el material. RF: "conservar el
     * progreso del estudiante y confirmar la finalización de cada actividad". Hoy no hace nada.
     */
    fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): devolver los desafíos del contenido (sin la evaluación final), ordenados
     * por dificultad progresiva. Hoy devuelve TODAS las actividades, sin filtrar ni ordenar.
     */
    fun desafiosDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): registrar la respuesta en el seguimiento del estudiante y devolver si fue
     * correcta (true/false) para mostrar la retroalimentación. Hoy devuelve null.
     */
    fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? = null

    /** TODO(equipo): devolver solo las preguntas de la evaluación final del contenido. Hoy devuelve todas. */
    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> = _actividades.value

    /**
     * TODO(equipo): intentos que le quedan al estudiante en la evaluación final (máximo definido
     * por el docente menos los usados). Regla de negocio: el máximo debe respetarse
     * estrictamente. Hoy devuelve null (sin calcular).
     */
    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? = null

    /**
     * TODO(equipo): registrar las respuestas de la evaluación final como un nuevo intento
     * (solo si quedan intentos) y actualizar el seguimiento. Hoy no hace nada.
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>) {
        // Sin implementar todavía.
    }

    /**
     * TODO(equipo): devolver las respuestas del estudiante en el contenido, para que revise
     * cuáles fueron correctas e incorrectas. RF: "revisar cuáles de sus respuestas fueron
     * correctas o incorrectas". Hoy devuelve una lista vacía.
     */
    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> = emptyList()
}
