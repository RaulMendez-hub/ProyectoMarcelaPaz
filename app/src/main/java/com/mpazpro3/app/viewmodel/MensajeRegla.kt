package com.mpazpro3.app.viewmodel

/**
 * Mensajes de reglas de negocio que el ViewModel comunica a la interfaz.
 *
 * El ViewModel no conoce Android ni textos: solo emite el tipo de mensaje. La capa de
 * presentación lo traduce con los recursos de `res/values/strings_validacion.xml`
 * (ver `ui/components/MensajesRegla.kt`).
 */
enum class MensajeRegla {
    CURSO_VACIO,
    SIN_CONTENIDOS,
    INTENTOS_INVALIDOS,
    CONTENIDOS_NO_AUTORIZADOS,
    ASIGNACION_REGISTRADA,
    INTENTOS_AGOTADOS,
    SIN_PREGUNTAS,
    RESPUESTAS_INCOMPLETAS
}
