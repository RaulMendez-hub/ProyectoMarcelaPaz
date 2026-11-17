package com.mpazpro3.app.ui.screen.docente

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.mpazpro3.app.ui.components.BarraSuperior
import com.mpazpro3.app.ui.components.recursoTexto
import com.mpazpro3.app.viewmodel.MpazProViewModel
import kotlinx.coroutines.launch

/**
 * Módulo "Planificación docente": el docente selecciona los objetivos/contenidos aprobados por
 * UTP, define el máximo de intentos de la evaluación final y los asigna a su curso.
 *
 * Las reglas de negocio (curso vacío, lista vacía, máximo de intentos >= 1, contenido no
 * autorizado) las valida el ViewModel; esta pantalla solo muestra el resultado en un Snackbar.
 */
@Composable
fun PlanificacionDocenteScreen(navController: NavHostController, viewModel: MpazProViewModel) {
    val usuario by viewModel.usuarioActivo.collectAsState()
    val todos by viewModel.contenidos.collectAsState()
    val disponibles = remember(todos) { viewModel.contenidosDisponiblesParaAsignar() }

    var seleccionados by remember { mutableStateOf(setOf<String>()) }
    var maxIntentos by remember { mutableIntStateOf(2) }

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

    val avanceSeleccion by animateFloatAsState(
        targetValue = if (disponibles.isEmpty()) 0f else seleccionados.size.toFloat() / disponibles.size,
        animationSpec = tween(durationMillis = 400),
        label = "avanceSeleccion"
    )

    Scaffold(
        topBar = { BarraSuperior("Planificación docente", navController) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("1. Selecciona los objetivos a trabajar", style = MaterialTheme.typography.titleSmall)

            AnimatedVisibility(
                visible = disponibles.isEmpty(),
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
                            text = stringResource(R.string.regla_lista_vacia),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            if (disponibles.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(disponibles) { contenido ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = contenido.id in seleccionados,
                                    onCheckedChange = { marcado ->
                                        seleccionados = if (marcado) seleccionados + contenido.id else seleccionados - contenido.id
                                    }
                                )
                                Column {
                                    Text(contenido.asignatura.etiqueta, style = MaterialTheme.typography.labelMedium)
                                    Text(contenido.objetivoAprendizaje, style = MaterialTheme.typography.bodyMedium)
                                    Text(contenido.estado.etiqueta, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }

            Text(
                "Seleccionados: ${seleccionados.size} de ${disponibles.size}",
                style = MaterialTheme.typography.labelMedium
            )
            LinearProgressIndicator(
                progress = { avanceSeleccion },
                modifier = Modifier.fillMaxWidth()
            )

            Text("2. Máximo de intentos de la evaluación final", style = MaterialTheme.typography.titleSmall)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = { if (maxIntentos > 1) maxIntentos-- }) { Text("−") }
                Text("$maxIntentos", style = MaterialTheme.typography.headlineSmall)
                OutlinedButton(onClick = { if (maxIntentos < 3) maxIntentos++ }) { Text("+") }
                if (maxIntentos >= 1) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.regla_intentos_ok),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                AnimatedVisibility(visible = maxIntentos < 1) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = stringResource(R.string.regla_intentos_invalidos),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            AnimatedVisibility(visible = maxIntentos < 1) {
                Text(
                    text = stringResource(R.string.regla_intentos_invalidos),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = seleccionados.isNotEmpty() && maxIntentos >= 1,
                onClick = {
                    val asignado = viewModel.asignarAlCurso(
                        curso = usuario?.curso.orEmpty(),
                        contenidoIds = seleccionados.toList(),
                        maxIntentos = maxIntentos
                    )
                    if (asignado) seleccionados = emptySet()
                }
            ) { Text("3. Asignar a mi curso") }
        }
    }
}
