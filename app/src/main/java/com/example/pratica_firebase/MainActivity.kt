package com.example.pratica_firebase

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import android.widget.Toast
import androidx.compose.ui.unit.dp
import com.example.pratica_firebase.ui.theme.PraticaFirebaseTheme
import com.example.pratica_firebase.cloud.FirebaseService
import com.example.pratica_firebase.model.UsuarioEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "FirestorePratica"
private val db = Firebase.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PraticaFirebaseTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Estados para los inputs del usuario
    val nombre = remember { mutableStateOf("") }
    val genero = remember { mutableStateOf("") }
    val estado = remember { mutableStateOf("") }
    val notificaciones = remember { mutableStateOf(false) }
    
    // Estados para Firebase Storage
    val archivoSeleccionado = remember { mutableStateOf<Uri?>(null) }
    val urlArchivo = remember { mutableStateOf("") }
    val subiendoArchivo = remember { mutableStateOf(false) }
    val nombreArchivo = remember { mutableStateOf("") }
    
    // Launcher para seleccionar archivos
    val seleccionarArchivoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            archivoSeleccionado.value = it
            nombreArchivo.value = it.lastPathSegment ?: "archivo_${System.currentTimeMillis()}"
            urlArchivo.value = ""
        }
    }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Guardar usuario en FireBase")

        OutlinedTextField(
            value = nombre.value,
            onValueChange = { nombre.value = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = genero.value,
            onValueChange = { genero.value = it },
            label = { Text("Género") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.value,
            onValueChange = { estado.value = it },
            label = { Text("Estado") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Checkbox(
                checked = notificaciones.value,
                onCheckedChange = { notificaciones.value = it }
            )
            Text(
                text = "Activar notificaciones",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        Button(
            onClick = {
                // Validación simple
                if (nombre.value.isBlank()) {
                    Toast.makeText(context, "Ingresa un nombre", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                val usuario = UsuarioEntity(
                    nombre = nombre.value,
                    genero = genero.value,
                    notificaciones = notificaciones.value,
                    estado = estado.value
                )

                FirebaseService.guardarUsuario(usuario)

                Toast.makeText(
                    context,
                    "Usuario guardado en Firestore",
                    Toast.LENGTH_SHORT
                ).show()

                // Limpiar campos
                nombre.value = ""
                genero.value = ""
                estado.value = ""
                notificaciones.value = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Guardar usuario")
        }
        
        // Separador visual
        Text(
            text = "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Text(
            text = "Firebase Storage",
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        // Botón para seleccionar archivo
        Button(
            onClick = {
                seleccionarArchivoLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Seleccionar archivo")
        }
        
        // Mostrar nombre del archivo seleccionado
        if (nombreArchivo.value.isNotEmpty()) {
            Text(
                text = "Archivo: ${nombreArchivo.value}",
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        // Botón para subir archivo
        if (archivoSeleccionado.value != null) {
            Button(
                onClick = {
                    if (archivoSeleccionado.value != null) {
                        subiendoArchivo.value = true
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val path = "archivos/${timestamp}_${nombreArchivo.value}"
                        
                        FirebaseService.subirArchivo(
                            fileUri = archivoSeleccionado.value!!,
                            path = path,
                            onSuccess = { url ->
                                urlArchivo.value = url
                                subiendoArchivo.value = false
                                Toast.makeText(
                                    context,
                                    "Archivo subido correctamente",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onFailure = { error ->
                                subiendoArchivo.value = false
                                Toast.makeText(
                                    context,
                                    "Error al subir archivo: ${error.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        )
                    }
                },
                enabled = !subiendoArchivo.value,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (subiendoArchivo.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Subiendo...")
                    }
                } else {
                    Text(text = "Subir archivo a Firebase Storage")
                }
            }
        }
        
        // Mostrar URL del archivo subido
        if (urlArchivo.value.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = "URL del archivo:",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                OutlinedTextField(
                    value = urlArchivo.value,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("URL de descarga") }
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PraticaFirebaseTheme {
        MainScreen()
    }
}
