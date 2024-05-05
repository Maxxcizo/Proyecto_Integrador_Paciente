package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class ReportActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var tituloText: EditText
    private lateinit var descripcionText: EditText
    private lateinit var frecuenciaInput: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_errores)

        toolbar = findViewById<Toolbar>(R.id.toolbar_reportarErrores)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        tituloText = findViewById(R.id.tituloReporte_et)
        descripcionText = findViewById(R.id.descripcionReporte_et)
        frecuenciaInput = findViewById(R.id.frecuencia_et)
        sendButton = findViewById(R.id.enviar_btn)

        val filter = InputFilter { source, _, _, _, _, _ ->
            val input = source.toString()
            if (input.isEmpty()) return@InputFilter null // Permite el borrado
            val pattern = Pattern.compile("[0-9]?")
            val matcher = pattern.matcher(input)
            if (matcher.matches()) null else ""
        }
        frecuenciaInput.filters = arrayOf(filter)

        toolbar.title = ""
        toolbarTitle.text = "Reporte de Errores"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        sendButton.setOnClickListener {
            if (validateFields()) {
                sendReport()
            }
        }
    }

    private fun validateFields(): Boolean {
        val titulo = tituloText.text.toString()
        val descripcion = descripcionText.text.toString()
        val frecuencia = frecuenciaInput.text.toString().toIntOrNull()

        if (titulo.isEmpty() || descripcion.isEmpty()) {
            Toast.makeText(this, "Favor de llenar todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (descripcion.length < 30) {
            descripcionText.error = "La descripción debe de contener al menos 30 caracteres"
            return false
        }

        if (frecuencia != null) {
            if (frecuencia > 10) {
                frecuenciaInput.error = "Ingresa un valor correcto 1-10"
                return false
            }
        }

        return true
    }

    private fun sendReport() {
        val titulo = tituloText.text.toString()
        val descripcion = descripcionText.text.toString()
        val frecuencia = frecuenciaInput.text.toString()

        // Obtener el correo del usuario si ha iniciado sesión
        val usuario = FirebaseAuth.getInstance().currentUser
        val correoUsuario = usuario?.email ?: "Correo del usuario no disponible"

        // Crear el cuerpo del correo electrónico
        val cuerpoCorreo = "Descripción del Problema:\n$descripcion\n\nNúmero de Veces que Ocurre: $frecuencia\n\nUsuario que hace el reporte: $correoUsuario"

        // Crear un Intent para enviar correo electrónico
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain" // Tipo de contenido del correo electrónico

        // Dirección de correo electrónico de soporte
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("isaacdaniel.chavez@gmail.com"))

        // Correo del usuario como copia oculta (CCO)
        intent.putExtra(Intent.EXTRA_BCC, arrayOf(correoUsuario))

        // Asunto del correo electrónico
        intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte de Error - $titulo")

        // Cuerpo del correo electrónico
        intent.putExtra(Intent.EXTRA_TEXT, cuerpoCorreo)

        // Especificar el paquete de la aplicación de Gmail
        intent.setPackage("com.google.android.gm")

        try {
            // Iniciar la actividad para enviar correo electrónico
            startActivity(intent)
        } catch (e: Exception) {
            // Manejar cualquier excepción que pueda ocurrir
            e.printStackTrace()

            // Si no se puede enviar el correo automáticamente, muestra un mensaje al usuario
            Toast.makeText(this, "No se pudo enviar el correo automáticamente. Por favor, elija una aplicación de correo para enviar el reporte.", Toast.LENGTH_SHORT).show()
        }

        // Después de enviar el reporte, puedes finalizar la actividad o mostrar un mensaje de confirmación
        finish()
    }

}
