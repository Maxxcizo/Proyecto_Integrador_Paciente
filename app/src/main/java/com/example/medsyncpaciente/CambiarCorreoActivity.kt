package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class CambiarCorreoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var correoActual: TextView
    private lateinit var nuevoCorreo: EditText
    private lateinit var cambiarCorreoButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cambiar_correo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar_cambiarcorreo)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        correoActual = findViewById(R.id.correoActual_tv)
        nuevoCorreo = findViewById(R.id.nuevoCorreo_et)
        cambiarCorreoButton = findViewById(R.id.cambiarCorreoButton)

        toolbar.title = ""
        toolbarTitle.text = "Cambiar Correo"
        setSupportActionBar(toolbar)

        val prefsCheck = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefsCheck.getString("email", null)
        correoActual.text = email

        auth = Firebase.auth

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        cambiarCorreoButton.setOnClickListener {
            val nuevo = nuevoCorreo.text.toString()

            if (!checkCorreoRequirements(nuevo)) {
                nuevoCorreo.error = "El correo electrónico debe tener un formato válido (ejemplo@dominio.com)."
                return@setOnClickListener
            }

            cambiarCorreoElectronico(nuevo)
        }
    }

    private fun cambiarCorreoElectronico(nuevoCorreo: String) {
        val user = auth.currentUser

        // Verifica si el usuario está autenticado
        if (user != null) {
            // Crear las credenciales de autenticación con el correo electrónico y la contraseña actual
            val credential = EmailAuthProvider.getCredential(user.email!!, "Isaac2005")

            // Reautenticar al usuario con las credenciales
            user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    // Actualizar el correo electrónico del usuario
                    user.updateEmail(nuevoCorreo).addOnCompleteListener { correoUpdateTask ->
                        if (correoUpdateTask.isSuccessful) {
                            // Actualizar el documento del usuario en Firestore
                            updateUsuarioEnFirestore(user.uid, nuevoCorreo)
                            showToast("Correo electrónico actualizado correctamente.")
                        } else {
                            showToast("No se pudo actualizar el correo electrónico: ${correoUpdateTask.exception?.message}")
                        }
                    }
                } else {
                    showToast("No se pudo reautenticar: ${reauthTask.exception?.message}")
                }
            }
        } else {
            showToast("Usuario no autenticado.")
        }
    }

    private fun updateUsuarioEnFirestore(userId: String, nuevoCorreo: String) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("Paciente").document(userId)

        userRef.update(mapOf(
            "Correo" to nuevoCorreo
            // Aquí puedes agregar otros campos que quieras actualizar en Firestore
        )).addOnSuccessListener {
            Log.d("DEBUG", "Correo actualizado en Firestore.")
        }.addOnFailureListener { e ->
            showToast("Error al actualizar el correo en Firestore: ${e.message}")
            Log.e("DEBUG", "Error al actualizar el correo en Firestore", e)
        }
    }

    private fun updateSharedPrefsCorreo(nuevoCorreo: String) {
        val prefsEdit = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefsEdit.putString("email", nuevoCorreo)
        prefsEdit.apply()
    }

    // Función para mostrar un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkCorreoRequirements(correo: String): Boolean {
        val regex = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return regex.matches(correo)
    }
}