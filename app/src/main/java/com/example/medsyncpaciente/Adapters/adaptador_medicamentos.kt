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
import com.example.medsyncpaciente.R

class AdaptadorMedicamentos(private val context: Context) : RecyclerView.Adapter<AdaptadorMedicamentos.ViewHolder>() {

    // hora,medicina,dosis

        private val medicamento = arrayOf("Paracetamol", "Ibuprofeno", "PeptoBismol")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_medicamentos, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicamento[position]

        holder.medicamentotext.text = medicine
        holder.card.setOnClickListener{
            // Acción a realizar cuando se haga clic en el botón "go"
            // Por ejemplo, puedes abrir una nueva actividad
            val intent = Intent(context, ConfirmarTomaActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicamento.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicamentotext: TextView = itemView.findViewById(R.id.medicamento_tv)
        var card: CardView = itemView.findViewById(R.id.medicamentos_cardview)
    }

}
