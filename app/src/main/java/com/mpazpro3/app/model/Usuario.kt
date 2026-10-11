package com.mpazpro3.app.model

/**
 * Usuario que inicia sesión. Según su perfil, AppNavigation lo lleva a su pantalla principal.
 * Solo datos ficticios (ver MockData.usuarios).
 *
 * Pendiente con el CITT: cómo se autentica el ESTUDIANTE (ver Consultas_Pendientes_Seccion007).
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val usuario: String,
    val clave: String,
    val perfil: Perfil,
    val curso: String? = null
)
