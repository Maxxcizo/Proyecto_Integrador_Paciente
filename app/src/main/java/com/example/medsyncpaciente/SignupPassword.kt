package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignupPassword : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var password_et: EditText
    private lateinit var passwordConfirmation_et: EditText
    private lateinit var registrar_btn: Button
    private lateinit var bd: FirebaseFirestore
    private lateinit var login_btn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bd = FirebaseFirestore.getInstance()

        backIcon = findViewById(R.id.back_btn)
        password_et = findViewById(R.id.et_password)
        passwordConfirmation_et = findViewById(R.id.et_ConfirmarContraseña)
        registrar_btn = findViewById(R.id.registrar_btn)
        login_btn = findViewById(R.id.login_tv)

        // Recuperar la información del intent de la actividad de registro
        val bundle = intent.extras
        val nombre = bundle?.getString("nombre") ?: ""
        val ap = bundle?.getString("ap") ?: ""
        val am = bundle?.getString("am") ?: ""
        val correo = bundle?.getString("correo") ?: ""
        val tel = bundle?.getString("tel") ?: ""

        setup(nombre, ap, am, correo, tel)
    }

    private fun setup(nombre: String, ap: String, am: String, correo: String, tel: String) {
        registrar_btn.setOnClickListener {
            val password = password_et.text.toString()
            val confirmPassword = passwordConfirmation_et.text.toString()

            var errorOccurred = false

            // Validar el campo de contraseña
            if (!checkPasswordRequirements(password)) {
                password_et.error = "La contraseña debe tener al menos 6 caracteres, incluyendo una letra mayúscula, una letra minúscula y un número."
                errorOccurred = true
            } else {
                password_et.error = null
            }

            // Validar el campo de confirmación de contraseña
            if (password != confirmPassword) {
                passwordConfirmation_et.error = "Las contraseñas no coinciden."
                errorOccurred = true
            } else {
                passwordConfirmation_et.error = null
            }

            // Si hubo un error, no continuamos con el registro
            if (errorOccurred) {
                return@setOnClickListener
            }

            // Si no hubo errores, continuamos con el registro
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener { createUserTask ->
                    if (createUserTask.isSuccessful) {
                        addPacientToFirestore(nombre, ap, am, correo, tel)
                        showHome(correo, password)
                    } else {
                        showAlert("Error al crear el usuario")
                    }
                }
        }
        backIcon.setOnClickListener {
            onBackPressed()
        }

        login_btn.setOnClickListener {
            val loginIntent = Intent(this, InicioDeSesion::class.java)
            startActivity(loginIntent)
        }
    }

    private fun addPacientToFirestore(nombre: String, ap: String, am: String, correo: String, tel: String) {
        val user = hashMapOf(
            "Nombre(s)" to nombre,
            "Apellido Paterno" to ap,
            "Apellido Materno" to am,
            "Correo" to correo,
            "Telefono" to tel
        )

        bd.collection("Paciente")
            .add(user)
            .addOnSuccessListener { documentReference ->
                // Handle success
                println("DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Handle errors
                println("Error adding document: $e")
            }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(correo: String, password: String) {
        // Iniciar la pantalla home y pasar el nombre
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", correo)
            putExtra("password", password)
        }
        startActivity(homeIntent)
    }

    private fun checkPasswordRequirements(password: String): Boolean {
        val regex = Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}\$")
        return regex.matches(password)
    }
}