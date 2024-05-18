package com.example.medsyncpaciente

import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorRegistroSintomas

class RegistroSintomasActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmarButton: Button
    private lateinit var adapter: AdaptadorRegistroSintomas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_sintomas)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configuración del RecyclerView y su adaptador
        recyclerView = findViewById(R.id.recycler_registroSintomas)

        // Se obtiene la instancia de los SharedPreferences
        val sharedPreferences = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        adapter = AdaptadorRegistroSintomas(this, sharedPreferences)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar = findViewById(R.id.toolbar_registroSintomas)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        confirmarButton = findViewById(R.id.confirmar_btn)

        toolbar.title = ""
        toolbarTitle.text = "Registro de Síntomas"
        setSupportActionBar(toolbar)

        adapter.cargarMediciones {
            // Todas las mediciones se han cargado, iniciar la siguiente actividad
            // Aquí puedes llamar al método para configurar el RecyclerView
            recyclerView.adapter = adapter
        }

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        confirmarButton.setOnClickListener {
            val dialogo1 = AlertDialog.Builder(this)
            dialogo1.setTitle("Síntomas")
            dialogo1.setMessage("¿Estás seguro de que la información es correcta?")
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
        adapter.guardarSintomasSeleccionados() // Guarda los síntomas seleccionados

        // Agregar la logica para el procesamiento de cuando se le cambie el tratamiento al paciente segun las especificaciones del medico

        finish()
    }

    private fun cancelar() {
        val t = Toast.makeText(this, "Rechazaste.", Toast.LENGTH_SHORT)
        t.show()
    }
}
