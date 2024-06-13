package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class Signup : AppCompatActivity() {

    lateinit var backIcon: ImageView
    lateinit var nombre_et: EditText
    lateinit var ap_et: EditText
    lateinit var am_et: EditText
    lateinit var correo_et: EditText
    lateinit var tel_et: EditText
    lateinit var continuar_btn: Button
    lateinit var login_btn: TextView
    lateinit var bd: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bd = FirebaseFirestore.getInstance()
        backIcon = findViewById(R.id.back_btn)
        nombre_et = findViewById(R.id.et_nombre)
        ap_et = findViewById(R.id.et_ap)
        am_et = findViewById(R.id.et_am)
        correo_et = findViewById(R.id.et_correo)
        tel_et = findViewById(R.id.et_telefono)
        continuar_btn = findViewById(R.id.continuar_btn)
        login_btn = findViewById(R.id.login_tv)

        setup()
        sesion()
    }

    private fun sesion() {
        // Agregar lo de shared Preferences para mantener la sesion iniciada al volver a abrir la aplicacion
    }

    fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        continuar_btn.setOnClickListener {
            val nombre = nombre_et.text.toString()
            val ap = ap_et.text.toString()
            val am = am_et.text.toString()
            val tel = tel_et.text.toString()
            val correo = correo_et.text.toString()

            var errorOccurred = false

            if (nombre.length !in 3..10) {
                errorOccurred = true
                nombre_et.error = "El nombre debe tener entre 3 y 10 caracteres."
            }

            if (ap.length !in 3..10) {
                errorOccurred = true
                ap_et.error = "El apellido paterno debe tener entre 3 y 10 caracteres."
            }

            if (am.length !in 3..10) {
                errorOccurred = true
                am_et.error = "El apellido materno debe tener entre 3 y 10 caracteres."
            }

            if (tel.length != 10) {
                errorOccurred = true
                tel_et.error = "El teléfono debe tener 10 dígitos."
            }

            if (!correo.matches(Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))) {
                errorOccurred = true
                correo_et.error = "El correo electrónico debe tener un formato válido (ejemplo@dominio.com)."
            }

            // Verificar si el correo electrónico ya está registrado
            bd.collection("Paciente").whereEqualTo("Correo", correo).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // El correo electrónico no está registrado, continuar con el registro
                        if (!errorOccurred) {
                            showSignupPassword(nombre, ap, am, correo, tel)
                        }
                    } else {
                        // El correo electrónico ya está registrado, mostrar mensaje de alerta
                        correo_et.error = "El correo electrónico ya está registrado"
                        errorOccurred = true
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar el error
                    Toast.makeText(this, "Error al verificar el correo electrónico: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

            // Si hubo un error, no continuamos con el registro
            if (errorOccurred) {
                return@setOnClickListener
            }
        }

        login_btn.setOnClickListener {
            val loginIntent = Intent(this, InicioDeSesion::class.java)
            startActivity(loginIntent)
        }
    }

    fun showSignupPassword(nombre: String, ap: String, am: String, correo: String, tel: String) {
        val SignUpPasswordIntent = Intent(this, SignupPassword::class.java).apply {
            putExtra("nombre", nombre)
            putExtra("ap", ap)
            putExtra("am", am)
            putExtra("correo", correo)
            putExtra("tel", tel)
        }
        Toast.makeText(this, "Se guardo la informacion", Toast.LENGTH_SHORT).show()
        startActivity(SignUpPasswordIntent)
    }
}
