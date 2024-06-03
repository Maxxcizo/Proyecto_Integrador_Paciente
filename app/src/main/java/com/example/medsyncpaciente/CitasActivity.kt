package com.example.medsyncpaciente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorCitas
import com.example.medsyncpaciente.Adapters.AdaptadorSettings
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CitasActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var botonFlotante: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_citas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        // Configuracion del Recycler View y su adaptador
        recyclerView = findViewById(R.id.recycler_citas)
        val adapter = AdaptadorCitas(this, prefs)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar = findViewById<Toolbar>(R.id.toolbar_citas)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        botonFlotante = findViewById<FloatingActionButton>(R.id.fab)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Citas"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        botonFlotante.setOnClickListener{
            startActivity(Intent(this, AddAppointmentActivity::class.java))
        }
    }
}