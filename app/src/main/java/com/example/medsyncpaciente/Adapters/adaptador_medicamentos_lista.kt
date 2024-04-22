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

class AdaptadorMedicamentosLista(private val context: Context) : RecyclerView.Adapter<AdaptadorMedicamentosLista.ViewHolder>() {

    // hora,medicina,dosis

    private val medicamentos = arrayOf("Paracetamol", "Ibuprofeno", "PeptoBismol")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_medicamentos_lista, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medicamentos[position]

        holder.medicamentostext.text = med

    }

    override fun getItemCount(): Int {
        return medicamentos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicamentostext: TextView = itemView.findViewById(R.id.medicamento_tv)
    }

}
