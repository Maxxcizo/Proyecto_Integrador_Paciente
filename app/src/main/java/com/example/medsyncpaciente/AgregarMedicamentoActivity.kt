package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.medsyncpaciente.Adapters.AdaptadorMedicamentos
import com.google.firebase.firestore.FirebaseFirestore

class AgregarMedicamentoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var agregarMedicamentoButton: Button
    private lateinit var editTextCantidad: EditText
    private lateinit var medicamentoText: TextView
    private lateinit var cantidadText: TextView
    private lateinit var diasText: TextView
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_medicamento)

        // Ajuste de las barras del sistema para mejorar la UI
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicialización de vistas y Firestore
        toolbar = findViewById(R.id.toolbar_agregarMedicamento)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        agregarMedicamentoButton = findViewById(R.id.agregarMedicamento_btn)
        backIcon = findViewById(R.id.back_btn)
        medicamentoText = findViewById(R.id.medicamento_tv)
        cantidadText = findViewById(R.id.cantidad_tv)
        diasText = findViewById(R.id.dias_tv)
        db = FirebaseFirestore.getInstance()

        // Obtener datos del intent
        val bundle = intent.extras
        val medicamento = bundle?.getString("medicamento")
        val cantidad = bundle?.getInt("cantidad")
        val dias = bundle?.getInt("dias")
        medicamentoText.text = medicamento
        cantidadText.text = cantidad.toString()+" tableta(s) restante(s)"
        diasText.text = dias.toString()+" día(s) restante(s)"

        Toast.makeText(this, "medicamento: $medicamento", Toast.LENGTH_SHORT).show()

        // Configuración de la toolbar
        toolbar.title = ""
        toolbarTitle.text = "Agregar Medicamento"
        setSupportActionBar(toolbar)

        // Configuración de listeners
        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        agregarMedicamentoButton.setOnClickListener {
            // Construir el AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ingrese la cantidad")

            // Inflar la vista de cantidad
            val layoutCantidad = layoutInflater.inflate(R.layout.dialog_cantidad, null) as LinearLayout
            editTextCantidad = layoutCantidad.findViewById(R.id.editTextCantidad)
            builder.setView(layoutCantidad)

            // Botón de aceptar
            builder.setPositiveButton("Aceptar") { dialog, _ ->
                aceptar()
            }

            // Botón de cancelar
            builder.setNegativeButton("Cancelar") { dialog, _ ->
                cancelar()
                dialog.cancel()
            }

            // Mostrar el AlertDialog
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun aceptar() {
        val cantidadAgregarStr = editTextCantidad.text.toString()
        if (cantidadAgregarStr.isNotEmpty()) {
            val cantidadAgregar = cantidadAgregarStr.toInt()
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val pacienteId = prefs.getString("pacienteId", null)

            val medicamento = medicamentoText.text.toString()

            if (pacienteId != null && medicamento.isNotEmpty()) {
                addMedicineToDatabase(medicamento, pacienteId, cantidadAgregar)
                finish()
            } else {
                Toast.makeText(this, "Algún campo está vacío.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "La cantidad no puede estar vacía.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelar() {
        Toast.makeText(this, "Rechazaste.", Toast.LENGTH_SHORT).show()
    }

    private fun addMedicineToDatabase(
        medicamento: String,
        pacienteId: String,
        cantidadAgregar: Int
    ) {

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        val medicamentoRef = db.collection("Paciente").document(pacienteId)
            .collection("Medicamentos").document(medicamento)

        println("Medicamento: $medicamento")
        println("Medicamento Reference: $medicamentoRef")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(medicamentoRef)
            val cantidadActual = snapshot.getLong("Cantidad") ?: 0L
            val nuevaCantidad = cantidadActual + cantidadAgregar

            transaction.update(medicamentoRef, "Cantidad", nuevaCantidad)
        }.addOnSuccessListener {
            Toast.makeText(this, "Medicamento actualizado correctamente", Toast.LENGTH_SHORT).show()

            // Aquí llamas al método para actualizar los medicamentos en el RecyclerView
            val adapter = AdaptadorMedicamentos(this, prefs)
            adapter.actualizarMedicamentos()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al actualizar medicamento: $e", Toast.LENGTH_SHORT).show()
            println("Error al actualizar medicamento: $e")
        }
    }

}
