package com.example.medsyncpaciente

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorCitas
import com.example.medsyncpaciente.Adapters.Cita
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class CitasActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var botonFlotante: FloatingActionButton
    private lateinit var btnCanceladas: Button
    private val citas = mutableListOf<Cita>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate: Iniciando CitasActivity")
        enableEdgeToEdge()
        setContentView(R.layout.activity_citas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar_citas)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        botonFlotante = findViewById(R.id.fab)
        backIcon = findViewById(R.id.back_btn)
        btnCanceladas = findViewById(R.id.btn_canceladas)

        toolbar.title = ""
        toolbarTitle.text = "Citas"
        setSupportActionBar(toolbar)

        // Recuperar citas desde Firestore antes de configurar el RecyclerView
        recuperarCitas()

        setup()
    }

    private fun recuperarCitas() {
        println("recuperarCitas: Iniciando recuperación de citas")
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pacienteID = prefs.getString("pacienteId", null)
        println("recuperarCitas: pacienteID = $pacienteID")
        val db = FirebaseFirestore.getInstance()

        if (pacienteID != null) {
            db.collection("Paciente").document(pacienteID).collection("Citas")
                .get()
                .addOnSuccessListener { result ->
                    println("recuperarCitas: Éxito al obtener citas")
                    val citasTemp = mutableListOf<Cita>()
                    for (document in result) {
                        println("recuperarCitas: Procesando documento de cita ${document.id}")
                        val citaIDRef = document.getDocumentReference("CitaID")
                        if (citaIDRef == null) {
                            println("recuperarCitas: citaIDRef es null para documento ${document.id}")
                            continue
                        }
                        citaIDRef.get()
                            .addOnSuccessListener { citaDocument ->
                                if (citaDocument.getBoolean("Cancelada") == true) {
                                    println("recuperarCitas: La cita ${citaDocument.id} está cancelada")
                                    return@addOnSuccessListener
                                }

                                val fechaHora = citaDocument.getString("Fecha_Hora") ?: ""
                                val medicoRef = citaDocument.getDocumentReference("MedicoID")
                                if (medicoRef == null) {
                                    println("recuperarCitas: medicoRef es null para documento ${citaDocument.id}")
                                    return@addOnSuccessListener
                                }

                                println("recuperarCitas: Éxito al obtener documento de cita ${citaDocument.id}")

                                // Obtener el nombre del médico
                                medicoRef.get()
                                    .addOnSuccessListener { medicoDocument ->
                                        val medicoID = medicoDocument.id
                                        val nombre = medicoDocument.getString("Nombre(s)") ?: ""
                                        val apellidoPaterno = medicoDocument.getString("Apellido Paterno") ?: ""
                                        val apellidoMaterno = medicoDocument.getString("Apellido Materno") ?: ""
                                        val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()
                                        println("recuperarCitas: Éxito al obtener datos del médico $nombreCompleto")

                                        citasTemp.add(Cita(document.id, nombreCompleto, fechaHora, citaIDRef, pacienteID, medicoID))
                                        // Ordenar la lista de citas por fecha después de agregar una nueva cita
                                        try {
                                            citasTemp.sortBy { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.fecha) }
                                        } catch (e: Exception) {
                                            println("recuperarCitas: Error al ordenar citas: ${e.message}")
                                        }
                                        // Actualizar la lista de citas y configurar el RecyclerView
                                        citas.clear()
                                        citas.addAll(citasTemp)
                                        configurarRecyclerView()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, "Error al obtener datos del médico: ${exception.message}")
                                        println("recuperarCitas: Error al obtener datos del médico: ${exception.message}")
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error al obtener datos de la cita: ${exception.message}")
                                println("recuperarCitas: Error al obtener datos de la cita: ${exception.message}")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al obtener citas: ${exception.message}")
                    println("recuperarCitas: Error al obtener citas: ${exception.message}")
                }
        } else {
            println("recuperarCitas: pacienteID es null")
        }
    }

    private fun configurarRecyclerView() {
        println("configurarRecyclerView: Configurando RecyclerView")
        recyclerView = findViewById(R.id.recycler_citas)
        val adapter = AdaptadorCitas(this, citas)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        println("configurarRecyclerView: RecyclerView configurado")
    }

    private fun setup() {
        println("setup: Configurando listeners")
        backIcon.setOnClickListener {
            println("setup: Click en backIcon")
            onBackPressed()
        }

        botonFlotante.setOnClickListener {
            println("setup: Click en botonFlotante")
            startActivity(Intent(this, AddAppointmentActivity::class.java))
        }

        btnCanceladas.setOnClickListener {
            println("setup: Click en btnCanceladas")
            startActivity(Intent(this, CanceledAppointmentsActivity::class.java))
        }
    }
}
