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

class AdaptadorSintomas(private val context: Context) : RecyclerView.Adapter<AdaptadorSintomas.ViewHolder>() {

    // hora,medicina,dosis

    private val sintomas = arrayOf("Dolor de cabeza", "Acidez Estomacal", "Secresion Nasal")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_sintomas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val syntoms = sintomas[position]

        holder.sintomastext.text = syntoms

    }

    override fun getItemCount(): Int {
        return sintomas.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sintomastext: TextView = itemView.findViewById(R.id.sintoma_tv)
    }

}
