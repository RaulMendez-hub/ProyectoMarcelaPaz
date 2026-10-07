package com.mpazpro3.app.model

/**
 * Asignación que hace el docente a su curso: qué contenido (ya autorizado por UTP) trabajará el
 * curso y cuántos intentos tiene la evaluación final.
 *
 * Regla de negocio: el máximo de intentos lo define el docente y debe respetarse estrictamente.
 */
data class Asignacion(
    val id: String,
    val curso: String,
    val contenidoId: String,
    val maxIntentosEvaluacion: Int
)
