package com.mpazpro3.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mpazpro3.app.R
import com.mpazpro3.app.model.Perfil
import com.mpazpro3.app.viewmodel.MpazProViewModel

/**
 * Inicio de sesión (primera pantalla). Según el perfil del usuario, AppNavigation lo lleva a
 * la pantalla principal de UTP, Docente o Estudiante.
 *
 * Validación por campo: usuario y clave se validan al escribir y al intentar ingresar; cada
 * campo muestra un ícono de error o de éxito y su mensaje (res/values/strings_validacion.xml).
 *
 * Usuarios de prueba (MockData.usuarios):
 *   utp@mpazpro3.cl / utp123
 *   docente.3basicoA@mpazpro3.cl / docente123
 *   est001 / 1234
 */
@Composable
fun LoginScreen(
    viewModel: MpazProViewModel,
    onIngresar: (Perfil) -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var claveVisible by remember { mutableStateOf(false) }
    var usuarioEditado by remember { mutableStateOf(false) }
    var claveEditada by remember { mutableStateOf(false) }
    var intentoEnvio by remember { mutableStateOf(false) }
    val error by viewModel.errorLogin.collectAsState()

    // Validación por campo: solo se muestra cuando el usuario ya escribió o intentó ingresar.
    val usuarioError = when {
        !(usuarioEditado || intentoEnvio) -> null
        usuario.isBlank() -> stringResource(R.string.validacion_usuario_vacio)
        usuario.trim().length < 3 -> stringResource(R.string.validacion_usuario_corto)
        else -> null
    }
    val claveError = when {
        !(claveEditada || intentoEnvio) -> null
        clave.isBlank() -> stringResource(R.string.validacion_clave_vacia)
        clave.length < 4 -> stringResource(R.string.validacion_clave_corta)
        else -> null
    }
    val formularioValido = usuario.trim().length >= 3 && clave.length >= 4

    // El ícono crece cuando el campo pasa a inválido y vuelve a su tamaño cuando es válido.
    val escalaUsuario by animateFloatAsState(
        targetValue = if (usuarioError == null) 1f else 1.2f,
        label = "escalaIconoUsuario"
    )
    val escalaClave by animateFloatAsState(
        targetValue = if (claveError == null) 1f else 1.2f,
        label = "escalaIconoClave"
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_escuela_respaldo),
                contentDescription = "Logo Escuela Escritora Marcela Paz",
                modifier = Modifier.size(110.dp)
            )
            Text("MPAZ PRO 3°", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Escuela Escritora Marcela Paz",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = usuario,
                onValueChange = {
                    usuario = it
                    usuarioEditado = true
                },
                label = { Text("Usuario o correo") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = usuarioError != null,
                supportingText = { if (usuarioError != null) Text(usuarioError) },
                trailingIcon = {
                    if (usuarioError != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = stringResource(R.string.validacion_campo_error),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.scale(escalaUsuario)
                        )
                    } else if (usuario.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = stringResource(R.string.validacion_campo_ok),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(escalaUsuario)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = clave,
                onValueChange = {
                    clave = it
                    claveEditada = true
                },
                label = { Text("Clave") },
                singleLine = true,
                isError = claveError != null,
                supportingText = { if (claveError != null) Text(claveError) },
                visualTransformation = if (claveVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (claveError != null) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = stringResource(R.string.validacion_campo_error),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.scale(escalaClave)
                            )
                        } else if (clave.isNotBlank()) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = stringResource(R.string.validacion_campo_ok),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.scale(escalaClave)
                            )
                        }
                        IconButton(onClick = { claveVisible = !claveVisible }) {
                            Icon(
                                imageVector = if (claveVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (claveVisible) "Ocultar clave" else "Mostrar clave"
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            AnimatedVisibility(
                visible = error != null,
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
                            imageVector = Icons.Filled.Error,
                            contentDescription = stringResource(R.string.validacion_campo_error),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = error.orEmpty(),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    intentoEnvio = true
                    if (formularioValido && viewModel.login(usuario.trim(), clave)) {
                        viewModel.usuarioActivo.value?.perfil?.let(onIngresar)
                    }
                }
            ) { Text("Ingresar") }

            // ---- SOLO PARA DESARROLLO ---- TODO(equipo): eliminar antes de entregar.
            Text(
                "Modo desarrollo (entrar sin login):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Perfil.entries.forEach { perfil ->
                TextButton(onClick = {
                    viewModel.entrarModoDesarrollo(perfil)
                    onIngresar(perfil)
                }) { Text("Entrar como ${perfil.etiqueta}") }
            }
        }
    }
}
