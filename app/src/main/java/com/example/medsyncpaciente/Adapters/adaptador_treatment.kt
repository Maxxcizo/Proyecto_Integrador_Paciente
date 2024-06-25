package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DataClases.Tratamiento
import com.example.medsyncpaciente.DetallesTratamiento
import com.example.medsyncpaciente.R
import java.text.SimpleDateFormat
import java.util.Locale

class AdaptadorTratamientos(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorTratamientos.ViewHolder>() {

    private var tratamientos = mutableListOf<Tratamiento>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato de fecha

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_treatment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val tratamiento = tratamientos[position]

            holder.medicotext.text = "Asignado por " + tratamiento.medico
            holder.fechaInicioText.text = tratamiento.fechaInicio
            holder.diagnosticoText.text = tratamiento.diagnostico

            holder.card.setOnClickListener {
                val intent = Intent(context, DetallesTratamiento::class.java).apply {
                    putExtra("TRATAMIENTO_ID", tratamiento.tratamientoID)
                    putExtra("TRATAMIENTO_REF", tratamiento.tratamientoRef.path)
                    putExtra("TRATAMIENTO_FECHA_INICIO", tratamiento.fechaInicio)
                    putExtra("TRATAMIENTO_FECHA_FIN", tratamiento.fechaFin)
                    putExtra("DIAGNOSTICO", tratamiento.diagnostico)
                    putExtra("SINTOMAS", tratamiento.sintomas.toTypedArray())
                    putExtra("RECOMENDACIONES", tratamiento.recomendaciones)
                    // Agregar otros extras necesarios
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar el tratamiento", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return tratamientos.size
    }

    fun actualizarLista(nuevaLista: List<Tratamiento>) {
        tratamientos.clear()
        tratamientos.addAll(nuevaLista)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicotext: TextView = itemView.findViewById(R.id.medico_tv)
        var fechaInicioText: TextView = itemView.findViewById(R.id.fecha_tv)
        var diagnosticoText: TextView = itemView.findViewById(R.id.diagnostico_tv)
        var card: CardView = itemView.findViewById(R.id.treatment_cardview)
    }
}
