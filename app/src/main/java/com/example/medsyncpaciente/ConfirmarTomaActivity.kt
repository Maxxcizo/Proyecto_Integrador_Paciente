package com.example.medsyncpaciente

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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class ConfirmarTomaActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var confirmarButton: Button
    private lateinit var hora_toma: TextView
    private lateinit var medicamentoNombre: TextView
    private lateinit var medicamentoCantidad: TextView
    private lateinit var medicamentoFrecuencia: TextView

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_confirmar_toma)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar_confirmartoma)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        confirmarButton = findViewById(R.id.confirmar_btn)
        hora_toma = findViewById(R.id.hora_tv)
        medicamentoNombre = findViewById(R.id.nombreMedicamento_tv)
        medicamentoCantidad = findViewById(R.id.cantidad_tv)
        medicamentoFrecuencia = findViewById(R.id.frecuencia_tv)

        val horaActual = obtenerHoraActual()
        hora_toma.text = horaActual

        val nombre = intent.getStringExtra("medicamento_nombre")
        val cantidad = intent.getIntExtra("medicamento_cantidad", 0)
        val frecuencia = intent.getIntExtra("medicamento_frecuencia", 0)

        medicamentoNombre.text = nombre
        medicamentoCantidad.text = "$cantidad tableta(s) restantes"
        medicamentoFrecuencia.text = "$frecuencia vez/veces al día"

        toolbar.title = ""
        toolbarTitle.text = "Realizar Toma"
        setSupportActionBar(toolbar)

        setup(nombre, horaActual)
    }

    private fun setup(nombre: String?, horaActual: String) {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        confirmarButton.setOnClickListener {
            if (nombre != null) {
                mostrarDialogoConfirmacion(nombre, horaActual)
            } else {
                Toast.makeText(this, "Error: Nombre del medicamento no encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoConfirmacion(nombre: String, horaActual: String) {
        AlertDialog.Builder(this).apply {
            setTitle("Confirmar Toma")
            setMessage("¿Está seguro que desea confirmar la toma del medicamento?")
            setPositiveButton("Sí") { dialog, _ ->
                registrarToma(nombre, horaActual)
                dialog.dismiss()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun registrarToma(nombre: String, horaActual: String) {
        val registro = hashMapOf(
            "Estado" to "completado",
            "Fecha_Hora" to horaActual
        )

        db.collection("Paciente")
            .document("F0JBxiZ6OVkS5gAcpWtM")
            .collection("Medicamentos")
            .document(nombre)
            .collection("Registro de Toma")
            .add(registro)
            .addOnSuccessListener {
                Toast.makeText(this, "Toma registrada exitosamente", Toast.LENGTH_SHORT).show()
                finish() // Cierra la actividad después de registrar la toma
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar la toma: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val fechaActual = Date()
        return formatoHora.format(fechaActual)
    }
}
