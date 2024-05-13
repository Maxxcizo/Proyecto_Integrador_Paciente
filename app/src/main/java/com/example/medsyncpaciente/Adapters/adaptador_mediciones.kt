package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class AdaptadorMediciones(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    // Instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Diccionario para encontrar las mediciones asignadas
    val diccionarioMediciones = listOf("Presion Arterial", "Glucosa en sangre", "Oxigenacion en Sangre", "Frecuencia Cardiaca")

    // Variables para almacenas las mediciones asignadas y sus frecuencias
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

        val claves = medicionesConFrecuencia.keys.toList()
        val valores = medicionesConFrecuencia.values.toList()

        val med = medicionesAsignadas.getOrNull(position)
        val frec = frecuenciaMedicionesAsignadas.getOrNull(position)
        if (claves != null) {
            holder.mediciontext.text = claves[position]
            holder.frecuenciatext.text = diccionarioFrecuencia.getOrDefault(valores[position], "")
            holder.horarioText.text = horario[position]
            diccionarioImageReferences[claves[position]]?.let {
                holder.iconoImage.setImageResource(
                    it
                )
            }
            holder.card.setOnClickListener {
                val intent = Intent(context, RegistroMedicionesActivity::class.java).apply {
                    putExtra("medicion", claves[position])
                    println("Mediciones asignadas: $medicionesAsignadas")
                    putExtra("frecuencia", diccionarioFrecuencia.getOrDefault(valores[position], ""))
                    println("Frecuencia asignada: $valores")
                    putExtra("hora", horario[position])
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return medicionesConFrecuencia.size
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

                            }
                            else{
                                println("El campo 'Frecuencia' no está presente en el documento: $medicion")
                            }
                        }else{
                            println("El documento no existe.")
                        }
                    }else {
                        println("Error al obtener el documento: ${task.exception}")
                    }

                    loadedMediciones++
                    // Verificar si todas las mediciones se han cargado
                    if (loadedMediciones == diccionarioMediciones.size) {
                        // Llamar al callback una vez que todas las mediciones se han cargado

                        // Iterar sobre las mediciones y sus frecuencias correspondientes
                        for ((index, medicion) in diccionarioMediciones.withIndex()) {
                            if (index < frecuenciaMedicionesAsignadas.size) {
                                val frecuencia = frecuenciaMedicionesAsignadas[index]
                                println("Medición: $medicion, Frecuencia: $frecuencia")
                                if (frecuencia != 0) {
                                    medicionesConFrecuencia[medicion] = frecuencia
                                }
                            } else {
                                println("No hay suficientes frecuencias asignadas para la medición: $medicion")
                                // Manejar la situación donde no hay suficientes frecuencias asignadas según sea necesario
                            }
                        }


                        callback()
                    }
                }
        }
    }
}
