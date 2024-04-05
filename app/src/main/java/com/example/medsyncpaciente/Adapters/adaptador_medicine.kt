package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.R

class AdaptadorMedicina(private val context: Context) : RecyclerView.Adapter<AdaptadorMedicina.ViewHolder>() {

    // hora,medicina,dosis

    private val hora = arrayOf("8:00 a.m.", "9:00 a.m.", "10:00 a.m.")
    private val medicamento = arrayOf("Paracetamol", "Ibuprofeno", "Agrifen")
    private val dosis = arrayOf("un par", "varios", "moderado")


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_medicine, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hour = hora[position]
        val medicine = medicamento[position]
        val dosis = dosis[position]

        holder.horatext.text = hour
        holder.medicamentotext.text = medicine
        holder.dosistext.text = dosis
        holder.goButton.setOnClickListener {
            // Acción a realizar cuando se haga clic en el botón "go"
            // Por ejemplo, puedes abrir una nueva actividad
            val intent = Intent(context, ConfirmarTomaActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return hora.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var horatext: TextView = itemView.findViewById(R.id.hour_tv)
        var medicamentotext: TextView = itemView.findViewById(R.id.medicamento_tv)
        var dosistext: TextView = itemView.findViewById(R.id.dosis_tv)
        var goButton: ImageView = itemView.findViewById(R.id.go_icon)
    }

}
