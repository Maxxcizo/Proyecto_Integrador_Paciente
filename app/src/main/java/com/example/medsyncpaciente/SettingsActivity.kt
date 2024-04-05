package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorSettings

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_settings)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        logoutButton = findViewById(R.id.logout_btn)

        // Configuracion del Recycler View y su adaptador
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView_Settings)
        val adapter = AdaptadorSettings(this)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar.title = ""
        toolbarTitle.text = "Settings"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }


        logoutButton.setOnClickListener{
            // Proceso de cerrar Sesion
        }

    }
}