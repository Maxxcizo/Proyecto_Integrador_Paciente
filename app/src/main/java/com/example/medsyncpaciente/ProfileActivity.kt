package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ProfileActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var saveButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_profile)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        saveButton = findViewById(R.id.save_btn)

        toolbar.title = ""
        toolbarTitle.text = "Perfil"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }


        saveButton.setOnClickListener{
            // Proceso de actualizar la informacion y mostrar la informacion del paciente en el card view
        }

    }
}