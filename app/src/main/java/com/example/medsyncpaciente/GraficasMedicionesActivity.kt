package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorGraficasMediciones
import com.github.mikephil.charting.data.Entry
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GraficasMedicionesActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var backIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graficas_mediciones)

        toolbar = findViewById(R.id.toolbar_graficasMediciones)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        recyclerView = findViewById(R.id.rv_graficasMediciones)
        backIcon = findViewById(R.id.back_btn)

        val adapter = AdaptadorGraficasMediciones(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar.title = ""
        toolbarTitle.text = "Gráficas Mediciones"
        setSupportActionBar(toolbar)

        fetchDataFromFirestore(adapter)

        //Agregar el onbackpressed para el backicon
        backIcon.setOnClickListener {
            onBackPressed()
        }
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
                    when (med) {
                        "Presion Arterial" -> {

                        }
                        else -> {

                        }
                    }
                    val fechas = mutableListOf<Date>()
                    val valoresS = mutableListOf<Float>()
                    val valoresD = mutableListOf<Float>()
                    val valoresF = mutableListOf<Float>()
                    val eventoAnomalosS = mutableListOf<Entry>()
                    val eventoAnomalosD = mutableListOf<Entry>()
                    val eventoAnomalosF = mutableListOf<Entry>()

                    for (document in documents) {
                        val fechaString = document.getString("Fecha y Hora") ?: ""
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val fecha = dateFormat.parse(fechaString)

                        val medicionAnomala = document.getBoolean("Evento Anomalo") ?: false

                        if (fecha != null) {
                            fechas.add(fecha)
                            val index = fechas.size - 1
                            if (med == "Presion Arterial") {
                                val valorS = document.getString("valor S")?.toFloat() ?: 0f
                                val valorD = document.getString("valor D")?.toFloat() ?: 0f
                                val valorF = document.getString("valor F")?.toFloat() ?: 0f

                                valoresS.add(valorS)
                                valoresD.add(valorD)
                                valoresF.add(valorF)

                                if (medicionAnomala) {
                                    eventoAnomalosS.add(Entry(index.toFloat(), valorS))
                                    eventoAnomalosD.add(Entry(index.toFloat(), valorD))
                                    eventoAnomalosF.add(Entry(index.toFloat(), valorF))
                                }
                            } else {
                                val valor = document.getString("valor")?.toFloat() ?: 0f
                                valoresS.add(valor)
                                if (medicionAnomala) {
                                    eventoAnomalosS.add(Entry(index.toFloat(), valor))
                                }
                            }
                        }
                    }

                    if (fechas.isNotEmpty() && valoresS.isNotEmpty()) {
                        val promediosDiariosS = calcularPromediosDiarios(fechas, valoresS)
                        val fechasPromedios = promediosDiariosS.keys.toList()
                        val valoresPromediosS = promediosDiariosS.values.toList()

                        val unidadMedida = obtenerUnidadDeMedida(med)
                        val rangoNormal = obtenerRangoNormal(med)

                        val medicion = if (med == "Presion Arterial") {
                            val promediosDiariosD = calcularPromediosDiarios(fechas, valoresD)
                            val promediosDiariosF = calcularPromediosDiarios(fechas, valoresF)

                            val valoresPromediosD = promediosDiariosD.values.toList()
                            val valoresPromediosF = promediosDiariosF.values.toList()

                            println("Valores promediosS: $valoresPromediosS")

                            println("Eventos anomalosS: $eventoAnomalosS")
                            println("Eventos anomalosD: $eventoAnomalosD")
                            println("Eventos anomalosF: $eventoAnomalosF")



                            AdaptadorGraficasMediciones.Medicion(
                                med,
                                fechasPromedios,
                                valoresPromediosS,
                                valoresPromediosD,
                                valoresPromediosF,
                                eventoAnomalosS,
                                eventoAnomalosD,
                                eventoAnomalosF,
                                unidadMedida,
                                rangoNormal
                            )
                        } else {

                            println("Valores promediosS: $valoresPromediosS")

                            println("Eventos anomalosS: $eventoAnomalosS")
                            println("Eventos anomalosD: $eventoAnomalosD")
                            println("Eventos anomalosF: $eventoAnomalosF")

                            AdaptadorGraficasMediciones.Medicion(
                                med,
                                fechasPromedios,
                                valoresPromediosS,
                                listOf(),
                                listOf(),
                                eventoAnomalosS,
                                listOf(),
                                listOf(),
                                unidadMedida,
                                rangoNormal
                            )
                        }

                        adapter.addMedicion(medicion)
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
            "Presion Arterial" -> 85f to 135f
            "Oxigenacion en Sangre" -> 90f to 100f
            else -> 0f to 0f
        }
    }
}
