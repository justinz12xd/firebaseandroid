package com.example.pratica_firebase.cloud

import android.util.Log
import com.example.pratica_firebase.model.UsuarioEntity
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Servicio sencillo para interactuar con Cloud Firestore.
 *
 * Asegúrate de haber configurado Firebase y agregado google-services.json
 * en la carpeta del módulo `app` antes de usar este servicio.
 */
object FirebaseService {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun guardarUsuario(usuario: UsuarioEntity) {
        val data = hashMapOf(
            "nombre" to usuario.nombre,
            "genero" to usuario.genero,
            "estado" to usuario.estado
        )

        db.collection("usuarios")
            .add(data)
            .addOnSuccessListener {
                Log.d("FirebaseService", "Usuario guardado correctamente en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al guardar en Firestore", e)
            }
    }

    fun obtenerUsuarios(callback: (List<UsuarioEntity>) -> Unit) {
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { doc ->
                    UsuarioEntity(
                        id = 0,
                        nombre = doc.getString("nombre") ?: "",
                        genero = doc.getString("genero") ?: "",
                        estado = doc.getString("estado") ?: ""
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener usuarios", e)
                callback(emptyList())
            }
    }
}


