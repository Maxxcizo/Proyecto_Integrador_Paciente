package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R

class AdaptadorSintomas(private val context: Context, private val sintomas: List<String>) : RecyclerView.Adapter<AdaptadorSintomas.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_sintomas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sintoma = sintomas[position]
        holder.sintomastext.text = sintoma
    }

    override fun getItemCount(): Int {
        return sintomas.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sintomastext: TextView = itemView.findViewById(R.id.sintoma_tv)
    }
}