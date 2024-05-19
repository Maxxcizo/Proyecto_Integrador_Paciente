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
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

// notas
// Corregir que al agregar una medicion de  presion arterial, se agregue correctamente, ya que se agrega
// un solo valor!

class RegistroMedicionesActivity : AppCompatActivity() {
    private lateinit var presionFragment: PresionFragment
    private lateinit var glucosaFragment: GlucosaFragment
    private lateinit var oxigenoFragment: OxigenoFragment
    private lateinit var frecuenciaFragment: FrecuenciaFragment
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
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
        // agregar otra pantalla para mostrar si el resultado esta dentro o fuera del promedio
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
            "Presion Arterial" -> mapOf(
                "valor S" to medicionesContent[0],
                "valor D" to medicionesContent[1],
                "valor F" to medicionesContent[2],
                "Fecha y Hora" to fechaHoraFormateada,
                "estado" to estado
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
                // Después de agregar la nueva medición, obtener las últimas 5 mediciones
                obtenerUltimas5Mediciones(measurementRef)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al agregar nueva medición: $e", Toast.LENGTH_SHORT).show()
                println("Nueva medición NO agregada correctamente")
            }
    }

    private fun obtenerUltimas5Mediciones(measurementRef: CollectionReference) {
        measurementRef.orderBy("Fecha y Hora", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val estados = querySnapshot.documents.mapNotNull { it.getString("estado") }
                println("El estado de las ultimas 5 mediciones> $estados")
                if (estados.size == 5 && estados.all { it == "Fuera del Rango" }) {
                    Toast.makeText(this, "Las últimas 5 mediciones están fuera de rango", Toast.LENGTH_LONG).show()
                    println("Las últimas 5 mediciones están fuera de rango")
                    // Agregar el funcionamiento para alertar al medico
                } else {
                    println("Las últimas mediciones no están fuera de rango")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al obtener mediciones: $e", Toast.LENGTH_SHORT).show()
                println("Error al obtener mediciones: $e")
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

                var media = 0
                var mediaS = 0
                var mediaD = 0
                var mediaF = 0

                var mediasPresion = mutableListOf<Int>()
                var estado = ""

                when(medicionType){
                    "Presion Arterial" -> {
                        if (medicionesSemanaValorS.isNotEmpty() && medicionesSemanaValorD.isNotEmpty() && medicionesSemanaValorF.isNotEmpty()) {
                            mediaS = medicionesSemanaValorS.average().roundToInt()
                            println("Promedio Mediciones S de la ultima semana: $mediaS")
                            mediaD = medicionesSemanaValorD.average().roundToInt()
                            println("Promedio Mediciones D de la ultima semana: $mediaD")
                            mediaF = medicionesSemanaValorF.average().roundToInt()
                            println("Promedio Mediciones F de la ultima semana: $mediaF")
                            mediasPresion.add(mediaS)
                            mediasPresion.add(mediaD)
                            mediasPresion.add(mediaF)

                            // Asegurarse de que `medicionesContent` tiene al menos 3 mediciones
                            if (medicionesContent.size >= 3) {
                                var fueraDeRango = false

                                for (i in 0..2) {
                                    val medicion = medicionesContent[i].toInt()
                                    val media = mediasPresion[i]

                                    if (medicion in (media - rango)..(media + rango)) {
                                        Toast.makeText(this, "La medición está dentro de la media", Toast.LENGTH_SHORT).show()
                                        println("Medicion dentro del rango: $medicion. Media $media")
                                    } else {
                                        Toast.makeText(this, "La medición está fuera de la media", Toast.LENGTH_SHORT).show()
                                        println("Medicion fuera del rango: $medicion. Media $media")
                                        fueraDeRango = true
                                    }
                                }

                                if (fueraDeRango) {
                                    estado = "Fuera del Rango"
                                } else {
                                    estado = "Dentro del Rango"
                                }

                                addMeasurementToDatabase(
                                    medicionType,
                                    medicionesContent,
                                    pacienteId,
                                    estado
                                )
                            }
                        } else {
                            estado = "Dentro del Rango"
                            addMeasurementToDatabase(
                                medicionType,
                                medicionesContent,
                                pacienteId,
                                estado
                            )
                        }
                    }
                    else -> {
                        if (medicionesSemana.isNotEmpty()) {
                            for (medicion in medicionesContent) {
                                // corregir eque no se compare
                                media = medicionesSemana.average().roundToInt()
                                println("Promedio Mediciones de la ultima semana: $media")

                                // Tratamiento por defecto para otros tipos de medición
                                if (medicion.toInt() in (media - rango)..(media + rango)) {
                                    Toast.makeText(
                                        this,
                                        "La medición está dentro de la media",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    println("Medicion dentro del rango: $medicion. Media $media")
                                    // Agregar las mediciones a la base de datos junto con el estado de la medicion
                                    estado = "Dentro del Rango"
                                    addMeasurementToDatabase(
                                        medicionType,
                                        medicionesContent,
                                        pacienteId,
                                        estado
                                    )
                                } else {
                                    Toast.makeText(
                                        this,
                                        "La medición está fuera de la media",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    println("Medicion fuera del rango: $medicion. Media $media")
                                    // Agregar las mediciones a la base de datos junto con el estado de la medicion
                                    estado = "Fuera del Rango"
                                    addMeasurementToDatabase(
                                        medicionType,
                                        medicionesContent,
                                        pacienteId,
                                        estado
                                    )
                                }
                            }
                        }
                        else{
                            estado = "Dentro del Rango"
                            addMeasurementToDatabase(
                                medicionType,
                                medicionesContent,
                                pacienteId,
                                estado
                            )
                        }
                    }
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
