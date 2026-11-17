package com.mpazpro3.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mpazpro3.app.R
import com.mpazpro3.app.viewmodel.MensajeRegla

/**
 * Relación entre la regla de negocio que emite el ViewModel y el texto que se muestra.
 * Así el ViewModel queda libre de recursos de Android y la pantalla decide cómo mostrarlo
 * (Snackbar o texto condicional).
 */
@StringRes
fun MensajeRegla.recursoTexto(): Int = when (this) {
    MensajeRegla.CURSO_VACIO -> R.string.regla_curso_vacio
    MensajeRegla.SIN_CONTENIDOS -> R.string.regla_sin_contenidos
    MensajeRegla.INTENTOS_INVALIDOS -> R.string.regla_intentos_invalidos
    MensajeRegla.CONTENIDOS_NO_AUTORIZADOS -> R.string.regla_contenido_no_autorizado
    MensajeRegla.ASIGNACION_REGISTRADA -> R.string.regla_asignacion_ok
    MensajeRegla.INTENTOS_AGOTADOS -> R.string.regla_intentos_agotados
    MensajeRegla.SIN_PREGUNTAS -> R.string.regla_sin_preguntas
    MensajeRegla.RESPUESTAS_INCOMPLETAS -> R.string.regla_respuestas_incompletas
}

@Composable
fun MensajeRegla.texto(): String = stringResource(recursoTexto())
