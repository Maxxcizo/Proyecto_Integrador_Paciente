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
import com.example.medsyncpaciente.Adapters.AdaptadorCumplimientoFecha
import com.example.medsyncpaciente.Adapters.AdaptadorMedicamentos
import com.example.medsyncpaciente.Adapters.AdaptadorMedicamentosLista
import com.example.medsyncpaciente.Adapters.AdaptadorMediciones
import com.example.medsyncpaciente.Adapters.AdaptadorMedicionesLista
import com.example.medsyncpaciente.Adapters.AdaptadorSintomas

class DetallesTratamiento : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerSintomas: RecyclerView
    private lateinit var recyclerMediciones: RecyclerView
    private lateinit var recyclerMedicamentos: RecyclerView
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
        val adapterSintomas = AdaptadorSintomas(this)

        val dividerItemDecorationSintomas = DividerItemDecoration(recyclerSintomas.context, DividerItemDecoration.VERTICAL)
        recyclerSintomas.addItemDecoration(dividerItemDecorationSintomas )

        recyclerSintomas.layoutManager = LinearLayoutManager(this)
        recyclerSintomas.adapter = adapterSintomas

        // Configuracion del Recycler View de mediciones y su adaptador
        recyclerMediciones = findViewById<RecyclerView>(R.id.recycler_Mediciones)
        val adapterMediciones = AdaptadorMedicionesLista(this)

        val dividerItemDecorationMediciones = DividerItemDecoration(recyclerMediciones.context, DividerItemDecoration.VERTICAL)
        recyclerMediciones.addItemDecoration(dividerItemDecorationMediciones )

        recyclerMediciones.layoutManager = LinearLayoutManager(this)
        recyclerMediciones.adapter = adapterMediciones



        // Configuracion del Recycler View de medicamentos y su adaptador
        recyclerMedicamentos = findViewById<RecyclerView>(R.id.recycler_Medicamentos)
        val adapterMedicamentos = AdaptadorMedicamentosLista(this)

        val dividerItemDecorationMedicamentos = DividerItemDecoration(recyclerMedicamentos.context, DividerItemDecoration.VERTICAL)
        recyclerMedicamentos.addItemDecoration(dividerItemDecorationMedicamentos )

        recyclerMedicamentos.layoutManager = LinearLayoutManager(this)
        recyclerMedicamentos.adapter = adapterMedicamentos




        toolbar = findViewById<Toolbar>(R.id.toolbar_detallesTratamiento)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Detalles Tratamiento"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }
    }
}