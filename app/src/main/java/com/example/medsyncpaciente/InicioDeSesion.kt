package com.example.medsyncpaciente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class InicioDeSesion : AppCompatActivity() {

    private lateinit var email_et: EditText
    private lateinit var password_et: EditText
    private lateinit var login_btn: Button
    private lateinit var signup_btn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        email_et = findViewById(R.id.email_et)
        password_et = findViewById(R.id.password_et)
        login_btn = findViewById(R.id.login_btn)
        signup_btn = findViewById(R.id.signUp_tv)

        // Configuración de la actividad
        setup()
        // Verificar si hay sesión activa
        checkSession()
    }

    private fun checkSession() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        if(email != null){
            showHome(email)
            finish() // Cerrar la actividad actual si la sesión está activa
        }
    }

    private fun setup() {
        title = "Autenticación"
        login_btn.setOnClickListener {
            val email = email_et.text.toString()
            val password = password_et.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showHome(email)
                            finish() // Cerrar la actividad actual si la sesión se inicia correctamente
                        } else {
                            showAlert("Se ha producido un error autenticando al usuario")
                        }
                    }
            } else {
                if (email.isEmpty()) {
                    email_et.error = "Por favor ingresa tu correo electrónico"
                }
                if (password.isEmpty()) {
                    password_et.error = "Por favor ingresa tu contraseña"
                }
            }
        }

        signup_btn.setOnClickListener{
            val signIntent = Intent(this, Signup::class.java)
            startActivity(signIntent)
        }
    }

    private fun showHome(email: String) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}
