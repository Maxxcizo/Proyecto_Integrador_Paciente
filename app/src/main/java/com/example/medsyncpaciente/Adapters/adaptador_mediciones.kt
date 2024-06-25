package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
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

class AdaptadorMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    private val diccionarioFrecuencia = mapOf(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    private val diccionarioImageReferences = mapOf(
        "Presion Arterial" to R.drawable.ic_blood_pressure,
        "Glucosa en sangre" to R.drawable.ic_blood_glucose,
        "Oxigenacion en Sangre" to R.drawable.ic_blood_oxygen,
        "Frecuencia Cardiaca" to R.drawable.ic_heart_rate
    )

    private val horario = arrayOf("1", "2", "3", "4")

    private var medicionesAsignadas = listOf<String>()
    private var frecuenciaMedicionesAsignadas = listOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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
                    putExtra("frecuencia", diccionarioFrecuencia.getOrDefault(frec, ""))
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

    fun setMediciones(mediciones: List<String>, frecuencias: List<Int>) {
        medicionesAsignadas = mediciones
        frecuenciaMedicionesAsignadas = frecuencias
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var frecuenciatext: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var horarioText: TextView = itemView.findViewById(R.id.horas_tv)
        var iconoImage: ImageView = itemView.findViewById(R.id.iconoMedicion)
        var card: CardView = itemView.findViewById(R.id.mediciones_cardview)
    }
}
