package com.mpazpro3.app.ui.screen.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mpazpro3.app.navigation.Rutas
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.viewmodel.MpazProViewModel

@Composable
fun EvaluacionFinalScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val contenidoId by viewModel.contenidoSeleccionadoId.collectAsState()
    val usuario by viewModel.usuarioActivo.collectAsState()
    val estudianteId = usuario?.id.orEmpty()
    val contenido = viewModel.contenidoPorId(contenidoId)
    val preguntas = remember(contenidoId) { viewModel.evaluacionFinalDe(contenidoId) }
    val intentos = viewModel.intentosRestantes(estudianteId, contenidoId.orEmpty())

    // Respuestas elegidas: id de la pregunta -> posición de la alternativa
    var respuestas by remember { mutableStateOf(mapOf<String, Int>()) }

    Scaffold(topBar = { BarraSuperior(contenido?.asignatura?.etiqueta ?: "Evaluación final", navController) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (intentos == null) "Intentos restantes: (pendiente de implementar)" else "Intentos restantes: $intentos",
                style = MaterialTheme.typography.titleMedium
            )

            if (preguntas.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = contenido?.unidad ?: "Sin contenido",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Todavía no hay evaluación final creada para esta asignatura.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Volver a desafíos", style = MaterialTheme.typography.titleMedium)
                }
                return@Column
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

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = respuestas.size == preguntas.size && preguntas.isNotEmpty() && (intentos == null || intentos > 0),
                onClick = {
                    viewModel.enviarEvaluacion(estudianteId, contenidoId.orEmpty(), respuestas)
                    navController.navigate(Rutas.ESTUDIANTE_REVISION)
                }
            ) { Text("Enviar evaluación", style = MaterialTheme.typography.titleMedium) }
        }
    }
}