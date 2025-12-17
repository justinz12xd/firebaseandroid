package com.example.pratica_firebase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    PraticaFirebaseTheme {
        MainScreen()
    }
}
