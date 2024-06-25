package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetallesCitaActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var cancelarButton: Button
    private lateinit var medicoTextView: TextView
    private lateinit var fechaTextView: TextView

    private lateinit var citaID: String
    private lateinit var citaFecha: String
    private lateinit var pacienteID: String
    private lateinit var citaRefPath: String
    private lateinit var nombreMedico: String
    private lateinit var medicoID: String // Agrega esta variable para el ID del médico
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalles_cita)

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar_detallesCita)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        cancelarButton = findViewById(R.id.cancelButton)
        medicoTextView = findViewById(R.id.medico_tv)
        fechaTextView = findViewById(R.id.fecha_tv)

        // Set up toolbar
        toolbar.title = ""
        toolbarTitle.text = "Detalles Cita"
        setSupportActionBar(toolbar)

        // Retrieve intent extras
        citaID = intent.getStringExtra("CITA_ID") ?: ""
        citaFecha = intent.getStringExtra("CITA_FECHA") ?: ""
        pacienteID = intent.getStringExtra("PACIENTE_ID") ?: ""
        citaRefPath = intent.getStringExtra("CITA_REF") ?: ""
        nombreMedico = intent.getStringExtra("NOMBRE_COMPLETO") ?: ""
        medicoID = intent.getStringExtra("MEDICO_ID") ?: "" // Obtén el ID del médico desde los extras

        medicoTextView.text = nombreMedico
        fechaTextView.text = citaFecha

        setupListeners()
    }

    private fun setupListeners() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        cancelarButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Aviso")
                .setMessage("¿Estás seguro de cancelar la cita?")
                .setCancelable(false)
                .setPositiveButton("Confirmar") { dialog, _ ->
                    cancelarCita()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun cancelarCita() {
        val fechaCita = dateFormat.parse(citaFecha)
        val fechaActual = Calendar.getInstance().time

        val diff = fechaCita.time - fechaActual.time
        val diffDays = diff / (1000 * 60 * 60 * 24)

        if (diffDays >= 1) {
            val db = FirebaseFirestore.getInstance()
            val citaRealRef = db.document(citaRefPath)

            val updates = hashMapOf<String, Any>("Cancelada" to true)

            citaRealRef.update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Cita cancelada correctamente.", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al cancelar la cita: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Solo se puede cancelar con mínimo 1 día de antelación.", Toast.LENGTH_SHORT).show()
        }
    }
}
