package com.example.medsyncpaciente

import android.content.Context
import android.content.Intent
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
import androidx.fragment.app.Fragment
import com.example.medsyncpaciente.fragments.MeasurementFragment
import com.example.medsyncpaciente.fragments.registromediciones.FrecuenciaFragment
import com.example.medsyncpaciente.fragments.registromediciones.GlucosaFragment
import com.example.medsyncpaciente.fragments.registromediciones.OxigenoFragment
import com.example.medsyncpaciente.fragments.registromediciones.PresionFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

class RegistroMedicionesActivity : AppCompatActivity() {
    private lateinit var presionFragment: PresionFragment
    private lateinit var glucosaFragment: GlucosaFragment
    private lateinit var oxigenoFragment: OxigenoFragment
    private lateinit var frecuenciaFragment: FrecuenciaFragment
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var registrarButton: Button
    private lateinit var posponerButton: Button
    private lateinit var confirmarButton: Button
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_mediciones)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        presionFragment = PresionFragment()
        glucosaFragment = GlucosaFragment()
        oxigenoFragment = OxigenoFragment()
        frecuenciaFragment = FrecuenciaFragment()

        toolbar = findViewById(R.id.toolbar_registroMediciones)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        registrarButton = findViewById(R.id.registrarMediciones_btn)
        posponerButton = findViewById(R.id.posponer_btn)
        confirmarButton = findViewById(R.id.confirmar_btn)

        db = FirebaseFirestore.getInstance()

        val bundle = intent.extras
        val medicion = bundle?.getString("medicion")

        Toast.makeText(this, "medicion: $medicion", Toast.LENGTH_SHORT).show()

        when (medicion) {
            "Presion Arterial" -> makeCurrentFragment(presionFragment)
            "Glucosa en sangre" -> makeCurrentFragment(glucosaFragment)
            "Oxigenacion en Sangre" -> makeCurrentFragment(oxigenoFragment)
            "Frecuencia Cardiaca" -> makeCurrentFragment(frecuenciaFragment)
            else -> Toast.makeText(this, "La opción no es válida", Toast.LENGTH_SHORT).show()
        }

        toolbar.title = ""
        toolbarTitle.text = "Registro de Mediciones"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        registrarButton.setOnClickListener {
            startActivity(Intent(this, RegistroSintomasActivity::class.java))
        }

        confirmarButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Aviso")
                setMessage("¿Seguro que quieres registrar tu medición?")
                setCancelable(false)
                setPositiveButton("Confirmar") { dialog, _ ->
                    aceptar()
                    dialog.dismiss()
                }
                setNegativeButton("Cancelar") { dialog, _ ->
                    cancelar()
                    dialog.dismiss()
                }
            }.show()
        }
    }

    private fun aceptar() {
        Toast.makeText(this, "Aceptaste.", Toast.LENGTH_SHORT).show()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pacienteId = prefs.getString("pacienteId", null)
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_wrapper)

        println("Current Fragment: $currentFragment")

        println("Paciente Id: $pacienteId")

        when (currentFragment) {
            is PresionFragment -> {
                println("PresionFragment detected.")
                val medicionesContent = currentFragment.getMedicionesContent()
                println("Mediciones Content: $medicionesContent")
                if (medicionesContent != null) {
                    if (validateMeasurements(medicionesContent)) {
                        // Consultar las mediciones de la última semana
                        consultarMedicionesSemanaAnterior(medicionesContent, "Presion Arterial", pacienteId)
                    }
                }
            }
            is GlucosaFragment -> {
                println("GlucosaFragment detected.")
                val medicionesContent = currentFragment.getMedicionesContent()
                println("Mediciones Content: $medicionesContent")
                if (medicionesContent != null) {
                    if (validateMeasurements(medicionesContent)) {
                        // Consultar las mediciones de la última semana
                        consultarMedicionesSemanaAnterior(medicionesContent, "Glucosa en sangre", pacienteId)
                    }
                }
            }
            is OxigenoFragment -> {
                println("OxigenoFragment detected.")
                val medicionesContent = currentFragment.getMedicionesContent()
                println("Mediciones Content: $medicionesContent")
                if (medicionesContent != null) {
                    println("Mediciones Content no es nulo")
                    if (validateMeasurements(medicionesContent)) {
                        // Consultar las mediciones de la última semana
                        consultarMedicionesSemanaAnterior(medicionesContent, "Oxigenacion en Sangre", pacienteId)
                    }
                }
            }
            is FrecuenciaFragment -> {
                println("FrecuenciaFragment detected.")
                val medicionesContent = currentFragment.getMedicionesContent()
                println("Mediciones Content: $medicionesContent")
                if (medicionesContent != null) {
                    if (validateMeasurements(medicionesContent)) {
                        // Consultar las mediciones de la última semana
                        consultarMedicionesSemanaAnterior(medicionesContent, "Frecuencia Cardiaca", pacienteId)
                    }
                }
            }
            else -> {
                println("Fragmento no válido.")
                Toast.makeText(this, "Fragmento no válido", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun validateMeasurements(medicionesContent: List<String>): Boolean {
        var isValid = true
        for (medicion in medicionesContent) {
            if (medicion.isEmpty()) {
                Toast.makeText(this, "Al menos un EditText está vacío", Toast.LENGTH_SHORT).show()
                isValid = false
                break
            }
            if (medicion.toInt() !in 0..200) {
                Toast.makeText(this, "Valores Fuera de Rango", Toast.LENGTH_SHORT).show()
                isValid = false
                break
            }
        }
        println("is valid? $isValid")
        return isValid
    }

    private fun addMeasurementToDatabase(
        measurementType: String,
        medicionesContent: List<String>,
        pacienteId: String?,
        estado: String?

    ) {
        println("tipo de medicion: $measurementType")
        val measurementRef = db.collection("Paciente").document(pacienteId!!)
            .collection("Mediciones").document(measurementType)
            .collection("Registro de Medicion")

        val fechaActual = Date()
        val formatoFechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val fechaHoraFormateada = formatoFechaHora.format(fechaActual)

        val nuevaMedicionData = when (measurementType) {
            "PresionFragment" -> mapOf(
                "valor S" to medicionesContent[0],
                "valor D" to medicionesContent[1],
                "valor F" to medicionesContent[2],
                "Fecha y Hora" to fechaHoraFormateada
            )
            else -> mapOf(
                "valor" to medicionesContent[0],
                "estado" to estado,
                "Fecha y Hora" to fechaHoraFormateada
            )
        }

        measurementRef.add(nuevaMedicionData)
            .addOnSuccessListener {
                Toast.makeText(this, "Nueva medición agregada correctamente", Toast.LENGTH_SHORT).show()
                println("Nueva medición agregada correctamente")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar nueva medición: $e", Toast.LENGTH_SHORT).show()
                println("Nueva medición NO agregada correctamente")
            }
    }

    private fun consultarMedicionesSemanaAnterior(medicionesContent: List<String>, medicionType: String, pacienteId: String?) {

        // Rango de tolerancia para la medición
        val rango = 15 // Rango de ±15 mg/dL

        // Calcular la fecha de hace 7 días
        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val fechaHaceUnaSemana = calendar.time
        val formatoFechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        // Formatear la fecha de hace 7 días al formato de tu base de datos
        val fechaHaceUnaSemanaString = formatoFechaHora.format(fechaHaceUnaSemana)

        println("fecha hace una semana: $fechaHaceUnaSemanaString")

        db.collection("Paciente").document(pacienteId!!)
            .collection("Mediciones").document(medicionType)
            .collection("Registro de Medicion")
            .whereGreaterThan("Fecha y Hora", fechaHaceUnaSemanaString)
            .get()
            .addOnSuccessListener { documents ->

                val medicionesSemana = mutableListOf<Int>()

                val medicionesSemanaValorS = mutableListOf<Int>()
                val medicionesSemanaValorD = mutableListOf<Int>()
                val medicionesSemanaValorF = mutableListOf<Int>()

                for (document in documents) {
                    when(medicionType){
                        "Presion Arterial" -> {
                            val valorS = document.getString("valor S")?.toInt()
                            valorS?.let {
                                medicionesSemanaValorS.add(it.toInt())
                            }
                            val valorD = document.getString("valor D")?.toInt()
                            valorD?.let {
                                medicionesSemanaValorD.add(it.toInt())
                            }
                            val valorF = document.getString("valor F")?.toInt()
                            valorF?.let {
                                medicionesSemanaValorF.add(it.toInt())
                            }
                        }
                        else -> {
                            // Obtener el valor de la medición
                            val valor = document.getString("valor")?.toInt()
                            valor?.let {
                                medicionesSemana.add(it.toInt())
                            }
                        }
                    }
                }

                println("Mediciones de la ultima semana: $medicionesSemana")

                println("Mediciones S de la ultima semana: $medicionesSemanaValorS")
                println("Mediciones D de la ultima semana: $medicionesSemanaValorD")
                println("Mediciones F de la ultima semana: $medicionesSemanaValorF")

                // Calcular la media de las mediciones de la última semana
                var media = 0
                if(medicionesSemana.isNotEmpty()){
                    media = medicionesSemana.average().roundToInt()


                    println("Promedio Mediciones de la ultima semana: $media")

                    // Comparar la nueva medición con la media
                    for(medicion in medicionesContent){
                        // Manejar diferentes tipos de medición
                        when (medicionType) {
                            "Presion arterial" -> {
                                // Comparar con los valores específicos de presión arterial
                                // Si es presión arterial, comparar los valores s, d y f/*
                                /*if (/* comparación de los valores s, d y f */) {
                                    Toast.makeText(this, "La medición de presión arterial está dentro de la media", Toast.LENGTH_SHORT).show()
                                    println("Medicion dentro del rango: $medicion")
                                } else {
                                    Toast.makeText(this, "La medición de presión arterial está fuera de la media", Toast.LENGTH_SHORT).show()
                                    println("Medicion fuera del rango: $medicion")
                                }*/
                            }
                            // Agregar más casos para otros tipos de medición si es necesario
                            else -> {
                                // Tratamiento por defecto para otros tipos de medición
                                if (medicion.toInt() in (media - rango)..(media + rango)) {
                                    Toast.makeText(this, "La medición está dentro de la media", Toast.LENGTH_SHORT).show()
                                    println("Medicion dentro del rango: $medicion")
                                    // Agregar las mediciones a la base de datos junto con el estado de la medicion
                                    val estado = "Dentro del Rango"
                                    addMeasurementToDatabase(medicionType, medicionesContent, pacienteId, estado)
                                } else {
                                    Toast.makeText(this, "La medición está fuera de la media", Toast.LENGTH_SHORT).show()
                                    println("Medicion fuera del rango: $medicion")
                                    // Agregar las mediciones a la base de datos junto con el estado de la medicion
                                    val estado = "Fuera de Rango"
                                    addMeasurementToDatabase(medicionType, medicionesContent, pacienteId, estado)
                                }
                            }
                        }
                    }
                }
                else{
                    // Agregar las mediciones a la base de datos junto con el estado de la medicion
                    val estado = "Dentro de Rango"
                    addMeasurementToDatabase(medicionType, medicionesContent, pacienteId, estado)
                }
            }
            .addOnFailureListener { e ->
                // Manejar el error
                Toast.makeText(this, "Error al consultar mediciones: $e", Toast.LENGTH_SHORT).show()
            }
    }


    private fun cancelar() {
        Toast.makeText(this, "Rechazaste.", Toast.LENGTH_SHORT).show()
    }
}
