package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.GraficasMedicionesActivity
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity

class AdaptadorMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    // hora,medicina,dosis

    private val medicion = arrayOf("Frecuencia Cardiaca", "Presión Arterial", "Oxigenacion en Sangre")
    private val frecuencia = arrayOf("90lpm", "20 mm/Hg", "92%")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medicion[position]
        val frec = frecuencia[position]

        holder.mediciontext.text = med
        holder.frecuenciatext.text = frec
        holder.card.setOnClickListener{
            // Acción a realizar cuando se haga clic en el botón "go"
            // Por ejemplo, puedes abrir una nueva actividad
            val intent = Intent(context, RegistroMedicionesActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicion.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var frecuenciatext: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var card: CardView = itemView.findViewById(R.id.mediciones_cardview)
    }

}
