package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignupPassword : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var password_et: EditText
    private lateinit var passwordConfirmation_et: EditText
    private lateinit var registrar_btn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        backIcon = findViewById(R.id.back_btn)
        password_et = findViewById(R.id.et_password)
        passwordConfirmation_et = findViewById(R.id.et_ConfirmarContraseña)
        registrar_btn = findViewById(R.id.registrar_btn)

        // Recuperar la informacion del intent de la actividad de registro
        val bundle = intent.extras
        val nombre = bundle?.getString("nombre") ?: ""
        val ap = bundle?.getString("ap") ?: ""
        val am = bundle?.getString("am") ?: ""
        val correo = bundle?.getString("correo") ?: ""
        val tel = bundle?.getString("tel") ?: ""

        setup(nombre, ap, am, correo, tel)
    }

    private fun setup(nombre: String, ap: String, am:String, correo:String, tel:String) {

        registrar_btn.setOnClickListener {
            // Verificar que los campos de las contraseñas no esten vacios
            if(password_et.text.isNotEmpty() && passwordConfirmation_et.text.isNotEmpty()){
                // Agregar las validaciones necesarias para las contraseñas
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    correo, password_et.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful){
                        showHome(nombre)
                    }else{
                        showAlert()
                    }
                }
            }
        }

        backIcon.setOnClickListener {
            onBackPressed()
        }



    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(nombre: String) {
        // Iniciar la pantalla home y pasar el email y el provedor
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("nombre", nombre)
        }
        startActivity(homeIntent)
    }
}