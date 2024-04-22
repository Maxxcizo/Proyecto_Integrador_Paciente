package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetallesCitaActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var cancelarButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles_cita)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_detallesCita)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        cancelarButton = findViewById(R.id.cancelButton)

        toolbar.title = ""
        toolbarTitle.text = "Detalles Cita"
        setSupportActionBar(toolbar)

        setup()
    }


    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        cancelarButton.setOnClickListener{
            val dialogo1 = AlertDialog.Builder(this)
            dialogo1.setTitle("Aviso")
            dialogo1.setMessage("¿Estás seguro de cancelar la cita?")
            dialogo1.setCancelable(false)
            dialogo1.setPositiveButton("Confirmar") { dialog, _ ->
                aceptar()
                dialog.dismiss()
            }
            dialogo1.setNegativeButton("Cancelar") { dialog, _ ->
                cancelar()
                dialog.dismiss()
            }
            dialogo1.show()
        }
    }

    private fun aceptar() {
        val t = Toast.makeText(this, "Aceptaste.", Toast.LENGTH_SHORT)
        t.show()
    }

    private fun cancelar() {
        val t = Toast.makeText(this, "Rechazaste.", Toast.LENGTH_SHORT)
        t.show()
    }
}