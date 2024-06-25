package com.example.medsyncpaciente.Adapters

import AdaptadorTomas
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DataClases.Medicamento
import com.example.medsyncpaciente.DetalleCumplimiento
import com.example.medsyncpaciente.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AdaptadorCumplimiento(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorCumplimiento.ViewHolder>() {

    private var medicamentos: List<Medicamento> = emptyList()

    fun setMedicamentos(medicamentos: List<Medicamento>) {
        this.medicamentos = medicamentos
        notifyDataSetChanged()
    }

    private val diccionarioFrecuencia = mapOf(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_cumplimiento, parent, false)
        return ViewHolder(v)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = medicamentos[position]
        val frecuencia = medicamento.frecuencia
        val cantidad = medicamento.cantidad

        holder.medicamentoText.text = medicamento.nombre
        holder.frecuenciaText.text = diccionarioFrecuencia[frecuencia]

        // Setear los días dinámicamente
        val hoy = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd")
        for (i in 0 until 7) {
            val dia = hoy.minusDays(i.toLong()).format(formatter)
            when (i) {
                0 -> holder.dia1.text = dia
                1 -> holder.dia2.text = dia
                2 -> holder.dia3.text = dia
                3 -> holder.dia4.text = dia
                4 -> holder.dia5.text = dia
                5 -> holder.dia6.text = dia
                6 -> holder.dia7.text = dia
            }
        }

        val adaptadorTomas = AdaptadorTomas(context, medicamento, sharedPreferences)
        holder.recyclerViewTomas.adapter = adaptadorTomas
        holder.recyclerViewTomas.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        holder.card.setOnClickListener {
            val intent = Intent(context, DetalleCumplimiento::class.java).apply {
                putExtra("medicamento", medicamento.nombre)
                putExtra("cantidad", cantidad)
                putExtra("dias", cantidad / frecuencia)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicamentos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicamentoText: TextView = itemView.findViewById(R.id.medicamento_tv)
        var frecuenciaText: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var card: CardView = itemView.findViewById(R.id.cumplimiento_cardview)
        var recyclerViewTomas: RecyclerView = itemView.findViewById(R.id.recycler_Tomas)
        var dia1: TextView = itemView.findViewById(R.id.dia1)
        var dia2: TextView = itemView.findViewById(R.id.dia2)
        var dia3: TextView = itemView.findViewById(R.id.dia3)
        var dia4: TextView = itemView.findViewById(R.id.dia4)
        var dia5: TextView = itemView.findViewById(R.id.dia5)
        var dia6: TextView = itemView.findViewById(R.id.dia6)
        var dia7: TextView = itemView.findViewById(R.id.dia7)
    }
}
