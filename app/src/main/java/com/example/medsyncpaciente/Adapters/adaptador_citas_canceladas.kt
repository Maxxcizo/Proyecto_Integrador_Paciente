package com.example.medsyncpaciente.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.AddAppointmentActivity
import com.example.medsyncpaciente.R
import java.text.SimpleDateFormat
import java.util.Locale

class AdaptadorCitasCanceladas(private val context: Context, private val citas: List<Cita>) : RecyclerView.Adapter<AdaptadorCitasCanceladas.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_citas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = citas[position]

        println("Citas Canceladas: $citas")

        holder.medicotext.text = cita.medico

        try {
            val date = dateFormat.parse(cita.fecha)
            holder.fechatext.text = cita.fecha
            println("Dia de la cita: ${date?.let { dayFormat.format(it) }}")
            println("Hora de la cita: ${date?.let { timeFormat.format(it) }}")
        } catch (e: Exception) {
            println("Dia de la cita no obtenido")
            println("Hora de la cita no obtenida")
        }

        holder.campoRecycler.setOnClickListener {
            // Mostrar diálogo de confirmación
            AlertDialog.Builder(context)
                .setTitle("Reprogramar Cita")
                .setMessage("¿Deseas reprogramar esta cita?")
                .setPositiveButton("Sí") { dialog, which ->
                    // Acción a realizar cuando se confirma la reprogramación
                    val intent = Intent(context, AddAppointmentActivity::class.java)
                    intent.putExtra("CITA_FECHA", cita.fecha)
                    intent.putExtra("MEDICO_ID", cita.medicoID)
                    context.startActivity(intent)
                }
                .setNegativeButton("No", null)
                .show()
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
