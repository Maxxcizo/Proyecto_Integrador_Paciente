package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Signup : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var nombre_et: EditText
    private lateinit var ap_et: EditText
    private lateinit var am_et: EditText
    private lateinit var correo_et: EditText
    private lateinit var tel_et: EditText
    private lateinit var continuar_btn: Button
    private lateinit var login_btn: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

    private fun setup() {

        backIcon.setOnClickListener {
            onBackPressed()
        }

        continuar_btn.setOnClickListener {
            // Verificar que los campos no esten vacios
            if(nombre_et.text.isNotEmpty() && ap_et.text.isNotEmpty() && am_et.text.isNotEmpty() && correo_et.text.isNotEmpty() && tel_et.text.isNotEmpty()){
                // Hacer las verificaciones para los diferentes campos del registro
                val nombre = nombre_et.text.toString()
                val ap = ap_et.text.toString()
                val am = am_et.text.toString()
                val correo = correo_et.text.toString()
                val tel = tel_et.text.toString()
                showSignupPassword(nombre, ap, am, correo, tel)

                // Agregar la información a la base de datos
            }
        }

        login_btn.setOnClickListener{
            val loginIntnet = Intent(this, LogIn::class.java)
            startActivity(loginIntnet)
        }
    }

    private fun showSignupPassword(nombre: String, ap: String, am:String, correo:String, tel:String) {
        val SignUpPasswordIntent = Intent(this, SignupPassword::class.java).apply {
            putExtra("nombre", nombre)
            putExtra("ap", ap)
            putExtra("am", am)
            putExtra("correo", correo)
            putExtra("tel", tel)
        }
        val toast = Toast.makeText(this, "Se guardo la informacion", Toast.LENGTH_SHORT)
        toast.show()

        startActivity(SignUpPasswordIntent)
    }
}