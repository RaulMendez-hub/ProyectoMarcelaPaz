package com.mpazpro3.app.viewmodel

import androidx.lifecycle.ViewModel
import com.mpazpro3.app.data.MockData
import com.mpazpro3.app.model.Actividad
import com.mpazpro3.app.model.Asignacion
import com.mpazpro3.app.model.Asignatura
import com.mpazpro3.app.model.Contenido
import com.mpazpro3.app.model.EstadoAprobacion
import com.mpazpro3.app.model.EstadoAvance
import com.mpazpro3.app.model.EstadoValidacion
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



/** ViewModel único, compartido por todas las pantallas (se crea una sola vez en AppNavigation). */
class MpazProViewModel : ViewModel() {

    // =====================================================================
    // Autenticación
    // =====================================================================

    private val _usuarioActivo = MutableStateFlow<Usuario?>(null)
    val usuarioActivo: StateFlow<Usuario?> = _usuarioActivo.asStateFlow()

    private val _errorLogin = MutableStateFlow<String?>(null)
    val errorLogin: StateFlow<String?> = _errorLogin.asStateFlow()

    /** Valida usuario y clave contra MockData.usuarios (el usuario no distingue mayúsculas). */
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

    /** Filtra por asignatura y por estado (null = todos). */
    fun contenidosFiltrados(asignatura: Asignatura?, estado: EstadoAprobacion?): List<Contenido> =
        _contenidos.value.filter {
            (asignatura == null || it.asignatura == asignatura) &&
                (estado == null || it.estado == estado)
        }

    private fun cambiarEstado(contenidoId: String, estado: EstadoAprobacion, observacion: String) {
        _contenidos.update { lista ->
            lista.map {
                if (it.id == contenidoId) it.copy(estado = estado, observacionUtp = observacion) else it
            }
        }
    }

    /** UTP autoriza el contenido y deja registrada su observación. */
    fun aprobarContenido(contenidoId: String, observacion: String) {
        cambiarEstado(contenidoId, EstadoAprobacion.APROBADO, observacion)
    }

    /** UTP rechaza el contenido y deja registrada su observación. */
    fun rechazarContenido(contenidoId: String, observacion: String) {
        cambiarEstado(contenidoId, EstadoAprobacion.RECHAZADO, observacion)
    }

    // =====================================================================
    // Módulo "Planificación docente"
    // =====================================================================

    /** Solo los contenidos APROBADOS por UTP pueden asignarse. */
    fun contenidosDisponiblesParaAsignar(): List<Contenido> =
        _contenidos.value.filter { it.estado == EstadoAprobacion.APROBADO }

    /**
     * Asigna contenidos al curso con el máximo de intentos de la evaluación final.
     * Si el contenido ya estaba asignado al curso, se actualiza el máximo de intentos
     * (no se duplica). Si alguno no está aprobado, no se asigna ninguno.
     */
    fun asignarAlCurso(curso: String, contenidoIds: List<String>, maxIntentos: Int) {
        if (curso.isBlank() || contenidoIds.isEmpty() || maxIntentos < 1) return

        val aprobados = _contenidos.value
            .filter { it.id in contenidoIds && it.estado == EstadoAprobacion.APROBADO }
        if (aprobados.size != contenidoIds.distinct().size) return // hay contenidos no autorizados

        _asignaciones.update { actuales ->
            val sinRepetidos = actuales.filterNot { it.curso == curso && it.contenidoId in contenidoIds }
            sinRepetidos + aprobados.map {
                Asignacion(
                    id = "AS-$curso-${it.id}",
                    curso = curso,
                    contenidoId = it.id,
                    maxIntentosEvaluacion = maxIntentos
                )
            }
        }
    }

    fun asignacionesDelCurso(curso: String): List<Asignacion> =
        _asignaciones.value.filter { it.curso == curso }

    /** Avance general (0.0 a 1.0): promedio del avance en los contenidos asignados a su curso. */
    fun progresoGeneral(estudianteId: String): Float {
        val asignados = contenidosAsignados(estudianteId)
        if (asignados.isEmpty()) return 0f
        return asignados.map { progreso(estudianteId, it.id) }.average().toFloat()
    }

    private val _resultadosCsv = MutableStateFlow<String?>(null)
    /** CSV listo para compartir; la pantalla lo comparte y luego llama a resultadosCsvConsumido(). */
    val resultadosCsv: StateFlow<String?> = _resultadosCsv.asStateFlow()

    fun resultadosCsvConsumido() {
        _resultadosCsv.value = null
    }

    /** Genera el CSV de resultados del curso (para compartir o guardar desde la pantalla). */
    fun descargarResultados(curso: String) {
        fun csv(texto: String) = "\"" + texto.replace("\"", "\"\"") + "\""
        val sb = StringBuilder("Curso,EstudianteID,Nombre,AvancePorcentaje,RespuestasCorrectas,RespuestasTotales\n")
        _estudiantes.value.filter { it.curso == curso }.forEach { e ->
            val (correctas, total) = resumenRespuestas(e.id)
            val avance = (progresoGeneral(e.id) * 100).toInt()
            sb.append("${csv(e.curso)},${csv(e.id)},${csv(e.nombreFicticio)},$avance,$correctas,$total\n")
        }
        _resultadosCsv.value = sb.toString()
    }

    // =====================================================================
    // Módulo "Informe pedagógico"
    // =====================================================================

    fun informeDe(estudianteId: String?): InformeDocente? = _informes.value.find { it.estudianteId == estudianteId }

    /**
     * Genera el informe en estado BORRADOR a partir de las respuestas del estudiante.
     * Sin diagnósticos ni consecuencias académicas: solo describe rendimiento, fortalezas
     * y aspectos a reforzar.
     */
    fun generarInforme(estudianteId: String) {
        val asignados = contenidosAsignados(estudianteId)
        val conDatos = asignados.map { it to respuestasDe(estudianteId, it.id) }.filter { it.second.isNotEmpty() }

        val correctas = conDatos.sumOf { (_, r) -> r.count { it.esCorrecta } }
        val total = conDatos.sumOf { (_, r) -> r.size }
        val rendimiento = if (total == 0) {
            "Aún no hay respuestas registradas para este estudiante."
        } else {
            "Respondió correctamente $correctas de $total preguntas (${correctas * 100 / total}%) " +
                "en ${conDatos.size} contenido(s) trabajado(s)."
        }

        val fortalezas = conDatos
            .filter { (_, r) -> r.count { it.esCorrecta } * 100 / r.size >= 70 }
            .map { (c, _) -> "Buen manejo de: ${c.unidad}" }
        val aReforzar = conDatos
            .filter { (_, r) -> r.count { it.esCorrecta } * 100 / r.size < 70 }
            .map { (c, _) -> "Reforzar: ${c.objetivoAprendizaje}" } +
            asignados.filter { a -> conDatos.none { it.first.id == a.id } }
                .map { "Aún sin actividades realizadas en: ${it.unidad}" }

        val informe = InformeDocente(
            id = "INF-$estudianteId",
            estudianteId = estudianteId,
            contenidoId = (conDatos.firstOrNull()?.first ?: asignados.firstOrNull())?.id.orEmpty(),
            rendimiento = rendimiento,
            fortalezas = fortalezas.ifEmpty { listOf("Sin fortalezas registradas todavía.") },
            aspectosAReforzar = aReforzar.ifEmpty { listOf("Sin aspectos a reforzar por ahora.") },
            fecha = SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")).format(Date()),
            estado = EstadoValidacion.BORRADOR
        )
        _informes.update { lista -> lista.filterNot { it.estudianteId == estudianteId } + informe }
    }

    /** Pasa el informe de BORRADOR a VALIDADO (validación docente). */
    fun validarInforme(informeId: String) {
        _informes.update { lista ->
            lista.map { if (it.id == informeId) it.copy(estado = EstadoValidacion.VALIDADO) else it }
        }
    }

    // =====================================================================
    // Módulo "Experiencia del estudiante"
    // =====================================================================

    /** Solo los contenidos aprobados y asignados al curso del estudiante. */
    fun contenidosDelEstudiante(usuario: Usuario?): List<Contenido> {
        if (usuario == null || usuario.perfil != Perfil.ESTUDIANTE) return emptyList()
        val curso = usuario.curso ?: return emptyList()
        val ids = _asignaciones.value.filter { it.curso == curso }.map { it.contenidoId }.toSet()
        return _contenidos.value.filter { it.id in ids && it.estado == EstadoAprobacion.APROBADO }
    }

    /**
     * Avance (0.0 a 1.0) en el contenido: material revisado + desafíos respondidos + evaluación
     * final enviada (1.0 al enviarla).
     */
    fun progreso(estudianteId: String, contenidoId: String): Float {
        val seg = seguimientoDe(estudianteId, contenidoId) ?: return 0f
        if (seg.estado == EstadoAvance.FINALIZADO) return 1f
        val desafios = desafiosDe(contenidoId)
        val respondidos = desafios.count { d -> seg.respuestas.any { it.actividadId == d.id } }
        val material = if (seg.estado != EstadoAvance.NO_INICIADO) 1 else 0
        val pasos = 1 + desafios.size + 1
        return (material + respondidos).toFloat() / pasos
    }

    /** Guarda que el estudiante terminó de revisar el material. */
    fun confirmarMaterialRevisado(estudianteId: String, contenidoId: String) {
        actualizarSeguimiento(estudianteId, contenidoId) {
            if (it.estado == EstadoAvance.NO_INICIADO) it.copy(estado = EstadoAvance.EN_PROGRESO) else it
        }
    }

    /** Desafíos del contenido (sin la evaluación final), de menor a mayor dificultad. */
    fun desafiosDe(contenidoId: String?): List<Actividad> =
        _actividades.value
            .filter { it.contenidoId == contenidoId && !it.esEvaluacionFinal }
            .sortedWith(compareBy({ it.dificultad.ordinal }, { it.orden }))

    /**
     * Registra la respuesta del desafío (numeroIntento = 0; si repite, reemplaza la anterior)
     * y devuelve si fue correcta. Devuelve null si la actividad o la alternativa no son válidas.
     */
    fun responderDesafio(estudianteId: String, actividad: Actividad, alternativa: Int): Boolean? {
        if (actividad.esEvaluacionFinal) return null
        val elegida = actividad.alternativas.getOrNull(alternativa) ?: return null
        val respuesta = RespuestaEstudiante(actividad.id, alternativa, elegida.esCorrecta, numeroIntento = 0)
        actualizarSeguimiento(estudianteId, actividad.contenidoId) { seg ->
            seg.copy(
                respuestas = seg.respuestas.filterNot { it.actividadId == actividad.id && it.numeroIntento == 0 } + respuesta,
                estado = if (seg.estado == EstadoAvance.NO_INICIADO) EstadoAvance.EN_PROGRESO else seg.estado
            )
        }
        return elegida.esCorrecta
    }

    /** Preguntas de la evaluación final del contenido. */
    fun evaluacionFinalDe(contenidoId: String?): List<Actividad> =
        _actividades.value
            .filter { it.contenidoId == contenidoId && it.esEvaluacionFinal }
            .sortedBy { it.orden }

    /** Intentos que le quedan: máximo del docente menos los usados (0 si el contenido no está asignado). */
    fun intentosRestantes(estudianteId: String, contenidoId: String): Int? {
        val asignacion = asignacionDe(estudianteId, contenidoId) ?: return 0
        val usados = seguimientoDe(estudianteId, contenidoId)?.intentosEvaluacionUsados ?: 0
        return (asignacion.maxIntentosEvaluacion - usados).coerceAtLeast(0)
    }

    /**
     * Registra un nuevo intento de la evaluación final, solo si quedan intentos y se
     * respondieron todas las preguntas.
     * @param respuestas mapa idActividad -> índice de la alternativa elegida.
     */
    fun enviarEvaluacion(estudianteId: String, contenidoId: String, respuestas: Map<String, Int>) {
        if ((intentosRestantes(estudianteId, contenidoId) ?: 0) <= 0) return
        val preguntas = evaluacionFinalDe(contenidoId)
        if (preguntas.isEmpty() || preguntas.any { it.id !in respuestas }) return

        actualizarSeguimiento(estudianteId, contenidoId) { seg ->
            val intento = seg.intentosEvaluacionUsados + 1
            val nuevas = preguntas.map { p ->
                val elegida = respuestas.getValue(p.id)
                RespuestaEstudiante(p.id, elegida, p.alternativas.getOrNull(elegida)?.esCorrecta == true, intento)
            }
            seg.copy(
                respuestas = seg.respuestas + nuevas,
                intentosEvaluacionUsados = intento,
                estado = EstadoAvance.FINALIZADO
            )
        }
    }

    /**
     * Respuestas vigentes del estudiante: la última de cada desafío y las del último intento
     * de la evaluación final.
     */
    fun respuestasDe(estudianteId: String, contenidoId: String): List<RespuestaEstudiante> {
        val seg = seguimientoDe(estudianteId, contenidoId) ?: return emptyList()
        val deDesafios = desafiosDe(contenidoId).mapNotNull { d ->
            seg.respuestas.lastOrNull { it.actividadId == d.id }
        }
        val ultimo = seg.intentosEvaluacionUsados
        val deEvaluacion = evaluacionFinalDe(contenidoId).mapNotNull { p ->
            seg.respuestas.lastOrNull { it.actividadId == p.id && it.numeroIntento == ultimo }
        }
        return deDesafios + deEvaluacion
    }
}
