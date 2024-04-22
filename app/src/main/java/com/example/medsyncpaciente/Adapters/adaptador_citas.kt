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
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.R

class AdaptadorCitas(private val context: Context) : RecyclerView.Adapter<AdaptadorCitas.ViewHolder>() {

    // hora,medicina,dosis

    private val medico = arrayOf("Isaac Chavez", "Daniel Atilano", "Mauricio Lopez")
    private val fecha = arrayOf("22/04/2024", "23/04/2024", "24/04/2024")
    private val hora = arrayOf("8:00 a.m.", "9:00 a.m.", "10:00 a.m.")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_citas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = medico[position]
        val fec = fecha[position]
        val hor = hora[position]

        holder.medicotext.text = med
        holder.fechatext.text = fec
        holder.horatext.text = hor
        holder.CampoRecycler.setOnClickListener{
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
        var medicotext: TextView = itemView.findViewById(R.id.medico_tv)
        var fechatext: TextView = itemView.findViewById(R.id.fecha_tv)
        var horatext: TextView = itemView.findViewById(R.id.hora_tv)
        var CampoRecycler: LinearLayout = itemView.findViewById(R.id.campoRecycler)
    }

}
