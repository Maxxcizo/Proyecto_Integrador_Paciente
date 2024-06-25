package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DetallesCitaActivity
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.DocumentReference
import java.text.SimpleDateFormat
import java.util.Locale

data class Cita(val citaID: String, val medico: String, val fecha: String, val citaRef: DocumentReference, val pacienteID:String, val medicoID: String)

class AdaptadorCitas(private val context: Context, private val citas: List<Cita>) : RecyclerView.Adapter<AdaptadorCitas.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_citas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = citas[position]

        holder.medicotext.text = cita.medico
        holder.fechatext.text = cita.fecha
        holder.campoRecycler.setOnClickListener {
            // Acción a realizar cuando se haga clic en el elemento de la lista
            val intent = Intent(context, DetallesCitaActivity::class.java)
            intent.putExtra("CITA_ID", cita.citaID)
            intent.putExtra("CITA_REF", cita.citaRef.path) // Pasar la referencia de la cita como una cadena
            intent.putExtra("CITA_FECHA", cita.fecha)
            intent.putExtra("PACIENTE_ID", cita.pacienteID) // Pasar pacienteID a DetallesCitaActivity
            intent.putExtra("NOMBRE_COMPLETO", cita.medico)
            intent.putExtra("MEDICO_ID", cita.medicoID)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return citas.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicotext: TextView = itemView.findViewById(R.id.medico_tv)
        var fechatext: TextView = itemView.findViewById(R.id.fecha_tv)
        var campoRecycler: LinearLayout = itemView.findViewById(R.id.campoRecycler)
    }
}
