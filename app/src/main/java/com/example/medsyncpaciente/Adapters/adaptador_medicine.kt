package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.fragments.Medicamento

class AdaptadorMedicina(
    private val context: Context,
    var medicamentos: List<Medicamento>
) : RecyclerView.Adapter<AdaptadorMedicina.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_medicine, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = medicamentos[position]

        holder.horatext.text = "Frecuencia: ${medicamento.frecuencia} vez/veces al día"
        holder.medicamentotext.text = medicamento.nombre
        holder.dosistext.text = "Cantidad: ${medicamento.cantidad} tableta(s)"

        holder.CampoRecycler.setOnClickListener {
            val intent = Intent(context, ConfirmarTomaActivity::class.java).apply {
                putExtra("medicamento_nombre", medicamento.nombre)
                putExtra("medicamento_cantidad", medicamento.cantidad)
                putExtra("medicamento_frecuencia", medicamento.frecuencia)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicamentos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var horatext: TextView = itemView.findViewById(R.id.hour_tv)
        var medicamentotext: TextView = itemView.findViewById(R.id.medicamento_tv)
        var dosistext: TextView = itemView.findViewById(R.id.dosis_tv)
        var CampoRecycler: LinearLayout = itemView.findViewById(R.id.campoRecycler)
    }
}
