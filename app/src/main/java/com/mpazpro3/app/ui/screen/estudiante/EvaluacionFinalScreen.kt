package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.R
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.recursoTexto
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * Evaluación final del contenido. RF: "responder la evaluación final dentro del número de
 * intentos permitidos". El máximo de intentos lo define el docente y el ViewModel lo hace
 * cumplir estrictamente: aquí solo se avisa con un mensaje cuando ya no quedan intentos.
 */
@Composable
fun EvaluacionFinalScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val estudianteId = usuario?.id.orEmpty()
    val seguimientos by viewModel.seguimientos.collectAsState() // recompone al cambiar los intentos
    val contenido = viewModel.contenidoPorId(contenidoId)
    val preguntas = remember(contenidoId) { viewModel.evaluacionFinalDe(contenidoId) }
    val intentos = remember(seguimientos, estudianteId, contenidoId) {
        viewModel.intentosRestantes(estudianteId, contenidoId.orEmpty())
    }

    // Respuestas elegidas: id de la pregunta -> posición de la alternativa
    var respuestas by remember { mutableStateOf(mapOf<String, Int>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val mensajeRegla by viewModel.mensajeRegla.collectAsState()
    val textoRegla = mensajeRegla?.let { stringResource(it.recursoTexto()) }

    // Muestra el mensaje de la regla de negocio una sola vez y lo consume enseguida.
    LaunchedEffect(mensajeRegla) {
        val texto = textoRegla ?: return@LaunchedEffect
        scope.launch { snackbarHostState.showSnackbar(texto) }
        viewModel.mensajeReglaMostrado()
    }

    val avanceRespuestas by animateFloatAsState(
        targetValue = if (preguntas.isEmpty()) 0f else respuestas.size.toFloat() / preguntas.size,
        animationSpec = tween(durationMillis = 300),
        label = "avanceRespuestas"
    )

    Scaffold(
        topBar = { BarraSuperior(contenido?.asignatura?.etiqueta ?: "Evaluación final", navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Intentos restantes: ${intentos ?: 0}",
                style = MaterialTheme.typography.titleMedium
            )
            LinearProgressIndicator(
                progress = { avanceRespuestas },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Respondidas: ${respuestas.size} de ${preguntas.size}",
                style = MaterialTheme.typography.labelMedium
            )

            AnimatedVisibility(
                visible = intentos != null && intentos <= 0,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(R.string.regla_intentos_agotados),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = preguntas.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = contenido?.unidad ?: stringResource(R.string.estado_vacio_titulo),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = stringResource(R.string.estado_vacio_evaluacion),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Intentos restantes: ${intentos ?: 0}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            preguntas.forEachIndexed { numero, pregunta ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${numero + 1}. ${pregunta.enunciado}", style = MaterialTheme.typography.titleMedium)
                        pregunta.alternativas.forEachIndexed { posicion, alternativa ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = respuestas[pregunta.id] == posicion,
                                    onClick = { respuestas = respuestas + (pregunta.id to posicion) }
                                )
                                Text(alternativa.texto, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }

            if (preguntas.isEmpty()) {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { navController.popBackStack() }
                ) {
                    Text(stringResource(R.string.accion_volver_desafios), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = respuestas.size == preguntas.size && (intentos ?: 0) > 0,
                    onClick = {
                        val enviado = viewModel.enviarEvaluacion(estudianteId, contenidoId.orEmpty(), respuestas)
                        if (enviado) navController.navigate(Rutas.ESTUDIANTE_REVISION)
                    }
                ) { Text("Enviar evaluación", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}
