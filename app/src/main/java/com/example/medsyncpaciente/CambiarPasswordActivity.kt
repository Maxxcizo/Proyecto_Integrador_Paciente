package com.example.medsyncpaciente

import android.os.Bundle
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
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class CambiarPasswordActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var passwordActual: EditText
    private lateinit var newPassword: EditText
    private lateinit var newPasswordConfirmation: EditText
    private lateinit var changepasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cambiar_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar_changepassword)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        passwordActual = findViewById(R.id.contraseñaactual_et)
        newPassword = findViewById(R.id.nuevacontraseña_et)
        newPasswordConfirmation = findViewById(R.id.confirmarcontraseña_et)
        changepasswordButton = findViewById(R.id.changepassword_btn)

        toolbar.title = ""
        toolbarTitle.text = "Cambiar Contraseña"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        changepasswordButton.setOnClickListener {
            val currentPassword = passwordActual.text.toString()
            val newPasswordText = newPassword.text.toString()
            val confirmnewPassword = newPasswordConfirmation.text.toString()

            var errorOccurred = false

            // Validar el campo de contraseña
            if (!checkPasswordRequirements(newPasswordText)) {
                newPassword.error = "La contraseña debe tener al menos 6 caracteres, incluyendo una letra mayúscula, una letra minúscula y un número."
                errorOccurred = true
            } else {
                newPassword.error = null
            }

            // Validar el campo de confirmación de contraseña
            if (newPasswordText != confirmnewPassword) {
                newPasswordConfirmation.error = "No coincide con la nueva contraseña."
                errorOccurred = true
            } else {
                newPasswordConfirmation.error = null
            }

            // Si hubo un error, no continuamos con el registro
            if (errorOccurred) {
                return@setOnClickListener
            }


            if (currentPassword.isNotEmpty()) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

                    user.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                user.updatePassword(newPasswordText)
                                    .addOnCompleteListener { passwordUpdateTask ->
                                        if (passwordUpdateTask.isSuccessful) {
                                            // Contraseña actualizada correctamente
                                            showToast("Contraseña actualizada correctamente.")
                                        } else {
                                            // No se pudo actualizar la contraseña
                                            showToast("No se pudo actualizar la contraseña. Inténtalo de nuevo más tarde.")
                                        }
                                    }
                            } else {
                                // La contraseña actual es incorrecta
                                passwordActual.error = "La contraseña actual es incorrecta. Inténtalo de nuevo."
                            }
                        }
                } else {
                    showToast("No se pudo obtener el usuario actual.")
                }
            } else {
                // Mostrar mensaje de error si los campos están vacíos
                showToast("Por favor ingresa la contraseña actual y la nueva contraseña.")
            }
        }
    }

    // Función para mostrar un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkPasswordRequirements(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}\$")
        return regex.matches(password)
    }
}
