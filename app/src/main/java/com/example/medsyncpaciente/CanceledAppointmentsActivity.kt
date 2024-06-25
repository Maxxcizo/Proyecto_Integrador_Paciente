package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorCitasCanceladas
import com.example.medsyncpaciente.Adapters.Cita
import com.google.firebase.firestore.FirebaseFirestore

class CanceledAppointmentsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView

    private lateinit var recyclerView: RecyclerView
    private val citasCanceladas = mutableListOf<Cita>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canceled_appointments)

        recyclerView = findViewById(R.id.recycler_citas_canceladas)
        toolbar = findViewById<Toolbar>(R.id.toolbar_citasCanceladas)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Citas Canceladas"
        setSupportActionBar(toolbar)

        backIcon.setOnClickListener {
            onBackPressed()
        }

        recuperarCitasCanceladas()
    }

    private fun recuperarCitasCanceladas() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pacienteID = prefs.getString("pacienteId", null)
        val db = FirebaseFirestore.getInstance()

        if (pacienteID != null) {
            db.collection("Paciente").document(pacienteID).collection("Citas")
                .get()
                .addOnSuccessListener { result ->
                    val citasTemp = mutableListOf<Cita>()
                    for (document in result) {
                        val citaIDRef = document.getDocumentReference("CitaID")
                        if (citaIDRef != null) {
                            citaIDRef.get()
                                .addOnSuccessListener { citaDocument ->
                                    if (citaDocument.getBoolean("Cancelada") == true) {
                                        val fechaHora = citaDocument.getString("Fecha_Hora") ?: ""
                                        val medicoRef = citaDocument.getDocumentReference("MedicoID")

                                        if (medicoRef != null) {
                                            medicoRef.get()
                                                .addOnSuccessListener { medicoDocument ->
                                                    val medicoID = medicoDocument.id
                                                    val nombre = medicoDocument.getString("Nombre(s)") ?: ""
                                                    val apellidoPaterno = medicoDocument.getString("Apellido Paterno") ?: ""
                                                    val apellidoMaterno = medicoDocument.getString("Apellido Materno") ?: ""
                                                    val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                                    citasTemp.add(Cita(document.id, nombreCompleto, fechaHora, citaIDRef, pacienteID, medicoID))
                                                    citasCanceladas.clear()
                                                    citasCanceladas.addAll(citasTemp)
                                                    configurarRecyclerView()
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
        }
    }

    private fun configurarRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = AdaptadorCitasCanceladas(this, citasCanceladas)
    }
}
