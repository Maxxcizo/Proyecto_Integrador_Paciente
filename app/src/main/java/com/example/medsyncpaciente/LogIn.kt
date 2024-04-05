package com.example.medsyncpaciente

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

class LogIn : AppCompatActivity() {

    private lateinit var email_et: EditText
    private lateinit var password_et: EditText
    private lateinit var login_btn: Button
    private lateinit var signup_btn: TextView

    // Agregar iniciacion de la base de datos
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

        //Configuracion
        setup()
    }

    private fun setup() {
        title = "Autenticacion"
        login_btn.setOnClickListener {
            if(email_et.text.isNotEmpty() && password_et.text.isNotEmpty()){
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                    email_et.text.toString(),
                    password_et.text.toString()
                ).addOnCompleteListener {
                    if(it.isSuccessful){
                        showHome(email_et.text.toString())
                    }else{
                        showAlert()
                    }
                }
            }
        }

        signup_btn.setOnClickListener{
            val signIntent = Intent(this, Signup::class.java)
            startActivity(signIntent)
        }
    }

    private fun showHome(email: String) {
        // Intent para iniciar la home activity y enviar el email
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(homeIntent)
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}