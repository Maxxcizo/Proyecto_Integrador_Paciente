package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
class AgregarMedicamentoActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var agregarMedicamentoButton: Button
    private lateinit var editTextCantidad: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_medicamento)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar_agregarMedicamento)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        agregarMedicamentoButton = findViewById(R.id.agregarMedicamento_btn)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Agregar Medicamento"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        agregarMedicamentoButton.setOnClickListener{
            // Construir el AlertDialog
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Ingrese la cantidad")

            // Inflar la vista de cantidad
            val layoutCantidad = layoutInflater.inflate(R.layout.dialog_cantidad, null) as LinearLayout
            editTextCantidad = layoutCantidad.findViewById(R.id.editTextCantidad)
            builder.setView(layoutCantidad)

            // Botón de aceptar
            builder.setPositiveButton("Aceptar") { dialog, which ->
                val cantidad = editTextCantidad.text.toString()
                // Aquí puedes hacer algo con la cantidad ingresada, como mostrarla en un Toast o guardarla en una variable
            }

            // Botón de cancelar
            builder.setNegativeButton("Cancelar") { dialog, which ->
                dialog.cancel()
            }

            // Mostrar el AlertDialog
            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
}
