package com.example.medsyncpaciente

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
import com.example.medsyncpaciente.Adapters.AdaptadorSintomas

class DetallesTratamiento : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerSintomas: RecyclerView
    private lateinit var medicoTratamiento: TextView
    private lateinit var diagnosticoTratamiento: TextView
    private lateinit var fechaTratamiento: TextView
    private lateinit var recomendacionesTratamiento: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalles_tratamiento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuracion del Recycler View de sintomas y su adaptador
        recyclerSintomas = findViewById<RecyclerView>(R.id.recycler_Sintomas)
        diagnosticoTratamiento = findViewById(R.id.diagnostico_tv)
        fechaTratamiento = findViewById(R.id.fecha_tv)
        recomendacionesTratamiento = findViewById(R.id.recomendaciones_tv)
        medicoTratamiento = findViewById(R.id.medico_tv)

        toolbar = findViewById<Toolbar>(R.id.toolbar_detallesTratamiento)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Detalles Tratamiento"
        setSupportActionBar(toolbar)

        setup()

        // Obtener los datos del intent
        val diagnostico = intent.getStringExtra("DIAGNOSTICO")
        val medico = intent.getStringExtra("NOMBRE_COMPLETO")
        val fecha = intent.getStringExtra("TRATAMIENTO_FECHA_INICIO")
        val sintomas = intent.getStringArrayExtra("SINTOMAS")?.toList() ?: emptyList()
        val recomendaciones = intent.getStringExtra("RECOMENDACIONES")

        // Asignar los valores a los elementos de la UI
        diagnosticoTratamiento.text = diagnostico
        medicoTratamiento.text = "Asignado por $medico"
        fechaTratamiento.text = fecha
        recomendacionesTratamiento.text = recomendaciones

        // Configurar el RecyclerView de síntomas
        val adapterSintomas = AdaptadorSintomas(this, sintomas)
        val dividerItemDecorationSintomas = DividerItemDecoration(recyclerSintomas.context, DividerItemDecoration.VERTICAL)
        recyclerSintomas.addItemDecoration(dividerItemDecorationSintomas)
        recyclerSintomas.layoutManager = LinearLayoutManager(this)
        recyclerSintomas.adapter = adapterSintomas
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}