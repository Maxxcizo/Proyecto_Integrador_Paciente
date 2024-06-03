package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DetallesCitaActivity
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Cita(val citaID: String, val medico: String, val fecha: String, val citaRef: DocumentReference)

class AdaptadorCitas(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorCitas.ViewHolder>() {

    private val citas = mutableListOf<Cita>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val pacienteID = sharedPreferences.getString("pacienteId", null)

    init {
        // Recuperar citas desde Firestore
        val db = FirebaseFirestore.getInstance()

        if (pacienteID != null) {
            db.collection("Paciente").document(pacienteID).collection("Citas")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val citaRef = document.getDocumentReference("CitaID") ?: continue
                        citaRef.get()
                            .addOnSuccessListener { citaDocument ->
                                val fechaHora = citaDocument.getString("Fecha_Hora") ?: ""
                                val medicoRef = citaDocument.getDocumentReference("MedicoID") ?: return@addOnSuccessListener

                                // Obtener el nombre del médico
                                medicoRef.get()
                                    .addOnSuccessListener { medicoDocument ->
                                        val nombre = medicoDocument.getString("Nombre") ?: ""
                                        val apellidoPaterno = medicoDocument.getString("Apellido Paterno") ?: ""
                                        val apellidoMaterno = medicoDocument.getString("Apellido Materno") ?: ""
                                        val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                        citas.add(Cita(document.id, nombreCompleto, fechaHora, citaRef))
                                        println("DocumentID: ${document.id} - citaRef: $citaRef")
                                        // Ordenar la lista de citas por fecha después de agregar una nueva cita
                                        citas.sortBy { dateFormat.parse(it.fecha) }
                                        notifyDataSetChanged() // Notificar al adaptador de los cambios
                                    }
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejo de errores
                    exception.printStackTrace()
                }
        }
    }

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
            intent.putExtra("PACIENTE_ID", pacienteID) // Pasar pacienteID a DetallesCitaActivity
            intent.putExtra("NOMBRE_COMPLETO", cita.medico)
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
