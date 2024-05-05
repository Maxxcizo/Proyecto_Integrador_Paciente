package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.GraficasMedicionesActivity
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity

class AdaptadorMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    // hora,medicina,dosis

    private val medicion = arrayOf("Presión Arterial", "Glucosa en Sangre", "Oxigenacion en Sangre", "Frecuencia Cardiaca")
    private val horario = arrayOf("8:00 a.m.", "9:00 a.m.", "10:00 a.m.", "11:00 a.m.")
    private val frecuencia = arrayOf("2 veces al día", "1 vez al día", "3 veces al día", "4 veces al día")
    private val imageReference = arrayOf(R.drawable.ic_blood_pressure, R.drawable.ic_blood_glucose, R.drawable.ic_blood_oxygen, R.drawable.ic_heart_rate)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medicion[position]
        val frec = frecuencia[position]
        val hor = horario[position]
        val ir = imageReference[position]

        holder.mediciontext.text = med
        holder.frecuenciatext.text = frec
        holder.horarioText.text = hor
        holder.iconoImage.setImageResource(ir)
        holder.card.setOnClickListener{
            // Acción a realizar cuando se haga clic en el botón "go"
            // Por ejemplo, puedes abrir una nueva actividad
            val intent = Intent(context, RegistroMedicionesActivity::class.java).apply {
                putExtra("medicion", med)
                putExtra("frecuencia", frec)
                putExtra("hora", hor)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicion.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var frecuenciatext: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var horarioText: TextView = itemView.findViewById(R.id.horas_tv)
        var iconoImage: ImageView = itemView.findViewById(R.id.iconoMedicion)
        var card: CardView = itemView.findViewById(R.id.mediciones_cardview)
    }

}
