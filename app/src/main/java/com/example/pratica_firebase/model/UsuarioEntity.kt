package com.example.pratica_firebase.model

/**
 * Representa un usuario de ejemplo para guardar en Firestore.
 * Más adelante puedes convertirla en una entidad de Room si lo necesitas.
 */
data class UsuarioEntity(
    val id: Int = 0,
    val nombre: String = "",
    val genero: String = "",
    val estado: String = ""
)


