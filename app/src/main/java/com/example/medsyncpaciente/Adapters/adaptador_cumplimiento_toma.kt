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

class AdaptadorCumplimientoToma(private val context: Context) : RecyclerView.Adapter<AdaptadorCumplimientoToma.ViewHolder>() {

    // hora,medicina,dosis

    private val fecha = arrayOf("vie 26/02/2024", "jue 25/02/2024", "lun 02/03/2024")
    private val tabletas = arrayOf("1 tableta(s)", "2 tableta(s)", "3 tableta(s)")
    private val estado = arrayOf("Realizado", "No Realizado", "Omitido")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_cumplimiento_toma, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val date = fecha[position]
        val tablet = tabletas[position]
        val state = estado[position]

        holder.fechatext.text = date
        holder.tabletastext.text = tablet
        holder.estadotext.text = state

        holder.lL.setOnClickListener{
            val intent = Intent(context, DetalleToma::class.java)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return fecha.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var fechatext: TextView = itemView.findViewById(R.id.fecha_tv)
        var tabletastext: TextView = itemView.findViewById(R.id.tabletas_tv)
        var estadotext: TextView = itemView.findViewById(R.id.estado_tv)
        var lL: LinearLayout = itemView.findViewById(R.id.Linear)
    }

}
