package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetalleToma : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_toma)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_detallesToma)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Detalles Toma"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}