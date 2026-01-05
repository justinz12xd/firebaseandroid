package com.example.pratica_firebase.cloud

import android.net.Uri
import android.util.Log
import com.example.pratica_firebase.model.UsuarioEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

/**
 * Servicio sencillo para interactuar con Cloud Firestore.
 *
 * Asegúrate de haber configurado Firebase y agregado google-services.json
 * en la carpeta del módulo `app` antes de usar este servicio.
 */
object FirebaseService {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    fun guardarUsuario(usuario: UsuarioEntity) {
        val data = hashMapOf(
            "nombre" to usuario.nombre,
            "genero" to usuario.genero,
            "estado" to usuario.estado,
            "notificaciones" to usuario.notificaciones
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
                        estado = doc.getString("estado") ?: "",
                        notificaciones = doc.getBoolean("notificaciones") ?: false
                    )
                }
                callback(lista)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener usuarios", e)
                callback(emptyList())
            }
    }

    /**
     * Sube un archivo a Firebase Storage
     * @param fileUri URI del archivo local a subir
     * @param path Ruta en Storage donde se guardará (ej: "imagenes/usuario123.jpg")
     * @param onSuccess Callback con la URL de descarga del archivo
     * @param onFailure Callback con el error si falla
     */
    fun subirArchivo(
        fileUri: Uri,
        path: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child(path)
        
        fileRef.putFile(fileUri)
            .addOnSuccessListener { taskSnapshot ->
                // Obtener la URL de descarga
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseService", "Archivo subido correctamente: ${uri.toString()}")
                    onSuccess(uri.toString())
                }.addOnFailureListener { e ->
                    Log.e("FirebaseService", "Error al obtener URL de descarga", e)
                    onFailure(e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al subir archivo", e)
                onFailure(e)
            }
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseService", "Progreso de subida: $progress%")
            }
    }

    /**
     * Descarga un archivo desde Firebase Storage
     * @param path Ruta del archivo en Storage
     * @param onSuccess Callback con la URL de descarga
     * @param onFailure Callback con el error si falla
     */
    fun obtenerUrlDescarga(
        path: String,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child(path)
        
        fileRef.downloadUrl
            .addOnSuccessListener { uri ->
                Log.d("FirebaseService", "URL de descarga obtenida: ${uri.toString()}")
                onSuccess(uri.toString())
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al obtener URL de descarga", e)
                onFailure(e)
            }
    }

    /**
     * Elimina un archivo de Firebase Storage
     * @param path Ruta del archivo en Storage
     * @param onSuccess Callback cuando se elimina correctamente
     * @param onFailure Callback con el error si falla
     */
    fun eliminarArchivo(
        path: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val fileRef = storageRef.child(path)
        
        fileRef.delete()
            .addOnSuccessListener {
                Log.d("FirebaseService", "Archivo eliminado correctamente: $path")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Error al eliminar archivo", e)
                onFailure(e)
            }
    }
}


