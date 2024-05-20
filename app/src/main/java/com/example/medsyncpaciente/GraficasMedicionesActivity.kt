package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorGraficasMediciones
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GraficasMedicionesActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas_mediciones)

        toolbar = findViewById(R.id.toolbar_graficasMediciones)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        recyclerView = findViewById(R.id.rv_graficasMediciones)

        val adapter = AdaptadorGraficasMediciones(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar.title = ""
        toolbarTitle.text = "Gráficas Mediciones"
        setSupportActionBar(toolbar)

        fetchDataFromFirestore(adapter)
    }

    private fun fetchDataFromFirestore(adapter: AdaptadorGraficasMediciones) {
        val db = FirebaseFirestore.getInstance()
        val medicionesAsignadas = mutableListOf<String>()
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pacienteId = prefs.getString("pacienteId", null)

        val medicionesRef = db.collection("Paciente").document(pacienteId!!).collection("Mediciones")

        medicionesRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val documentId = document.id
                    medicionesAsignadas.add(documentId)
                }

                fetchMediciones(medicionesAsignadas, adapter, pacienteId)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Error al consultar mediciones: $exception",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun fetchMediciones(medicionesAsignadas: List<String>, adapter: AdaptadorGraficasMediciones, pacienteId: String) {
        val db = FirebaseFirestore.getInstance()

        val calendar = Calendar.getInstance()
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val fechaHaceUnaSemana = calendar.time
        val formatoFechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        val fechaHaceUnaSemanaString = formatoFechaHora.format(fechaHaceUnaSemana)

        for (med in medicionesAsignadas) {
            db.collection("Paciente").document(pacienteId)
                .collection("Mediciones").document(med)
                .collection("Registro de Medicion")
                .whereGreaterThan("Fecha y Hora", fechaHaceUnaSemanaString)
                .get()
                .addOnSuccessListener { documents ->
                    val fechas = mutableListOf<Date>()
                    val valores = mutableListOf<Float>()

                    for (document in documents) {
                        val fechaString = document.getString("Fecha y Hora") ?: ""
                        val valor = document.getString("valor")?.toFloat() ?: 0f
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val fecha = dateFormat.parse(fechaString)

                        if (fecha != null) {
                            fechas.add(fecha)
                            valores.add(valor)
                        }
                    }

                    if (fechas.isNotEmpty() && valores.isNotEmpty()) {
                        val promediosDiarios = calcularPromediosDiarios(fechas, valores)
                        val fechasPromedios = promediosDiarios.keys.toList()
                        val valoresPromedios = promediosDiarios.values.toList()
                        val unidadMedida = obtenerUnidadDeMedida(med)
                        val rangoNormal = obtenerRangoNormal(med)
                        val medicion = AdaptadorGraficasMediciones.Medicion(med, fechasPromedios, valoresPromedios, unidadMedida, rangoNormal)
                        adapter.addMedicion(medicion) // Agregar medición al adaptador
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error al consultar mediciones: $exception",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun calcularPromediosDiarios(fechas: List<Date>, valores: List<Float>): Map<Date, Float> {
        val mapaMediciones = mutableMapOf<String, MutableList<Float>>()
        val formatoFechaOutput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (i in fechas.indices) {
            val fechaFormateada = formatoFechaOutput.format(fechas[i])
            if (!mapaMediciones.containsKey(fechaFormateada)) {
                mapaMediciones[fechaFormateada] = mutableListOf()
            }
            mapaMediciones[fechaFormateada]?.add(valores[i])
        }

        val promediosDiarios = mutableMapOf<Date, Float>()
        for ((fecha, valoresDelDia) in mapaMediciones) {
            val promedio = valoresDelDia.average().toFloat()
            promediosDiarios[SimpleDateFormat("yyyy-MM-dd").parse(fecha)] = promedio
        }

        return promediosDiarios
    }

    private fun obtenerUnidadDeMedida(medicion: String): String {
        return when (medicion) {
            "Frecuencia Cardiaca" -> "lpm"
            "Glucosa en sangre" -> "mg/dl"
            "Presion Arterial" -> "mm / hg"
            "Oxigenacion en Sangre" -> "%"
            else -> ""
        }
    }

    private fun obtenerRangoNormal(medicion: String): Pair<Float, Float> {
        return when (medicion) {
            "Frecuencia Cardiaca" -> 60f to 100f
            "Glucosa en sangre" -> 70f to 140f
            "Presion Arterial" -> 90f to 120f
            "Oxigenacion en Sangre" -> 90f to 100f
            else -> 0f to 0f
        }
    }
}
