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
import com.example.medsyncpaciente.DetallesTratamiento
import com.example.medsyncpaciente.R

class AdaptadorTratamientos(private val context: Context) : RecyclerView.Adapter<AdaptadorTratamientos.ViewHolder>() {

    // hora,medicina,dosis

    private val diagnostico = arrayOf("Diabetes", "Hipertension", "Migraña")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_treatment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diagnostico = diagnostico[position]

        holder.diag.text = diagnostico
        holder.card.setOnClickListener{
            // Acción a realizar cuando se haga clic en el botón "go"
            // Por ejemplo, puedes abrir una nueva actividad
            val intent = Intent(context, DetallesTratamiento::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return diagnostico.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var diag: TextView = itemView.findViewById(R.id.diagnostico_tv)
        var card: CardView = itemView.findViewById(R.id.treatment_cardview)
    }

}
