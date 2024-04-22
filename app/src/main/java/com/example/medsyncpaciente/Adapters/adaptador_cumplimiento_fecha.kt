package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DetalleCumplimiento
import com.example.medsyncpaciente.DetalleToma
import com.example.medsyncpaciente.R

class AdaptadorCumplimientoFecha(private val context: Context) : RecyclerView.Adapter<AdaptadorCumplimientoFecha.ViewHolder>() {

    // hora,medicina,dosis
    private val fecha = arrayOf("Febrero 2024", "Marzo 2024", "Abril 2024")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_cumplimiento_mes, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = fecha[position]
        holder.fechatext.text = date

        val adapter = AdaptadorCumplimientoToma(context)
        holder.recycler.layoutManager = LinearLayoutManager(context)
        holder.recycler.adapter = adapter

    }

    override fun getItemCount(): Int {
        return fecha.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fechatext: TextView = itemView.findViewById(R.id.fecha_tv)
        var recycler: RecyclerView = itemView.findViewById(R.id.recyclerView_CumplimientoToma)
    }
}
