package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.DetalleCumplimiento
import com.example.medsyncpaciente.DetalleToma
import com.example.medsyncpaciente.R

class AdaptadorMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    // hora,medicina,dosis

    private val medicion = arrayOf("Frecuencia Cardiaca", "Presión Arterial", "Oxigenacion en Sangre")
    private val valor = arrayOf("90lpm", "20 mm/Hg", "92%")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medicion[position]
        val value = valor[position]

        holder.mediciontext.text = med
        holder.valortext.text = value
    }

    override fun getItemCount(): Int {
        return medicion.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var valortext: TextView = itemView.findViewById(R.id.valor_tv)
    }

}
