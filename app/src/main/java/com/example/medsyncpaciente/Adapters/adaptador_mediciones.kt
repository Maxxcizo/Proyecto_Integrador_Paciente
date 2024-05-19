package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity
import com.google.firebase.firestore.FirebaseFirestore

class AdaptadorMediciones(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    // Instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Diccionario para encontrar las mediciones asignadas
    val diccionarioMediciones = listOf("Presion Arterial", "Glucosa en sangre", "Oxigenacion en Sangre", "Frecuencia Cardiaca")

    // Variables para almacenar las mediciones asignadas y sus frecuencias
    var medicionesAsignadas = mutableListOf<String>()
    var frecuenciaMedicionesAsignadas = mutableListOf<Int>()

    // Diccionario que almacena las mediciones con su frecuencia
    val medicionesConFrecuencia = mutableMapOf<String, Int?>()

    // Diccionario para relacionar la frecuencia y asignar el texto correspondiente
    private val diccionarioFrecuencia = mapOf<Int, String?>(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    // Diccionario para relacionar las mediciones con las imagenes de cada medicion
    private val diccionarioImageReferences = mapOf(
        "Presion Arterial" to R.drawable.ic_blood_pressure,
        "Glucosa en sangre" to R.drawable.ic_blood_glucose,
        "Oxigenacion en Sangre" to R.drawable.ic_blood_oxygen,
        "Frecuencia Cardiaca" to R.drawable.ic_heart_rate
    )

    private val horario = arrayOf("1", "2", "3", "4")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("Mediciones asignadas: $medicionesConFrecuencia")

        val med = medicionesAsignadas.getOrNull(position)
        val frec = frecuenciaMedicionesAsignadas.getOrNull(position)

        if (med != null && frec != null) {
            holder.mediciontext.text = med
            holder.frecuenciatext.text = diccionarioFrecuencia.getOrDefault(frec, "")
            holder.horarioText.text = horario[position]
            diccionarioImageReferences[med]?.let {
                holder.iconoImage.setImageResource(it)
            }
            holder.card.setOnClickListener {
                val intent = Intent(context, RegistroMedicionesActivity::class.java).apply {
                    putExtra("medicion", med)
                    println("Mediciones asignadas: $medicionesAsignadas")
                    putExtra("frecuencia", diccionarioFrecuencia.getOrDefault(frec, ""))
                    println("Frecuencia asignada: $frec")
                    putExtra("hora", horario[position])
                }
                context.startActivity(intent)
            }
        } else {
            Toast.makeText(context, "No hay suficiente información para la medición en la posición $position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return medicionesAsignadas.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var frecuenciatext: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var horarioText: TextView = itemView.findViewById(R.id.horas_tv)
        var iconoImage: ImageView = itemView.findViewById(R.id.iconoMedicion)
        var card: CardView = itemView.findViewById(R.id.mediciones_cardview)
    }

    // Método para cargar las mediciones desde Firestore
    fun cargarMediciones(callback: () -> Unit) {
        val pacienteId = sharedPreferences.getString("pacienteId", null)
        var loadedMediciones = 0 // Contador para verificar todas las mediciones cargadas

        for (medicion in diccionarioMediciones) {
            val frecuenciaCardiacaRef =
                db.collection("Paciente").document(pacienteId.toString()).collection("Mediciones")
                    .document(medicion)

            frecuenciaCardiacaRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            val frecuencia = document.getLong("Frecuencia")
                            medicionesAsignadas.add(medicion)
                            if (frecuencia != null) {
                                frecuenciaMedicionesAsignadas.add(frecuencia.toInt())
                                println("La frecuencia de $medicion es: $frecuencia")

                                medicionesConFrecuencia[medicion] = frecuencia.toInt()
                            } else {
                                println("El campo 'Frecuencia' no está presente en el documento: $medicion")
                            }
                        } else {
                            println("El documento no existe.")
                        }
                    } else {
                        println("Error al obtener el documento: ${task.exception}")
                    }

                    loadedMediciones++
                    // Verificar si todas las mediciones se han cargado
                    if (loadedMediciones == diccionarioMediciones.size) {
                        callback()
                    }
                }
        }
    }
}
