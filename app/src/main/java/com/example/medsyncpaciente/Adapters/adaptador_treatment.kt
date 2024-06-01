package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DetallesTratamiento
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorTratamientos(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorTratamientos.ViewHolder>() {

    // Instancia de la base de datos Firestore
    private val db = FirebaseFirestore.getInstance()

    // Lista para almacenar los datos de los tratamientos
    private val tratamientos = mutableListOf<Tratamiento>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_treatment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tratamiento = tratamientos[position]
        holder.diag.text = tratamiento.diagnostico
        holder.medico.text = "Asignado por ${tratamiento.medico}"
        holder.fecha.text = tratamiento.fechaAsignacion

        holder.card.setOnClickListener {
            val intent = Intent(context, DetallesTratamiento::class.java).apply {
                putExtra("diagnostico", tratamiento.diagnostico)
                putExtra("medico", tratamiento.medico)
                putExtra("fecha", tratamiento.fechaAsignacion)
                putExtra("sintomas", tratamiento.sintomas.toTypedArray())  // Pasar los síntomas como array
                putExtra("recomendaciones", tratamiento.recomendaciones)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = tratamientos.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var diag: TextView = itemView.findViewById(R.id.diagnostico_tv)
        var medico: TextView = itemView.findViewById(R.id.medico_tv)
        var fecha: TextView = itemView.findViewById(R.id.fecha_tv)
        var card: CardView = itemView.findViewById(R.id.treatment_cardview)
    }

    data class Tratamiento(
        val medico: String,
        val fechaAsignacion: String,
        val diagnostico: String,
        val sintomas: List<String>,
        val recomendaciones: String
    )

    fun cargarTratamientos(callback: () -> Unit) {
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId != null) {
            val tratamientosRef = db.collection("Paciente").document(pacienteId).collection("Tratamientos")

            tratamientosRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        val tempTratamientos = mutableListOf<Tratamiento>()
                        var remainingTasks = documents.size()

                        for (document in documents) {
                            val medicoRef = document.getDocumentReference("MedicoID")
                            val fechaInicio = document.getString("Fecha_Inicio") ?: ""
                            val diagnostico = document.getString("Diagnostico") ?: ""
                            val sintomas = document.get("Sintomas") as? List<String> ?: emptyList()
                            val recomendaciones = document.getString("Recomendaciones") ?: ""

                            if (medicoRef != null) {
                                medicoRef.get().addOnCompleteListener { medicoTask ->
                                    if (medicoTask.isSuccessful) {
                                        val medicoDocument = medicoTask.result
                                        if (medicoDocument != null && medicoDocument.exists()) {
                                            val nombreMedico = medicoDocument.getString("Nombre") ?: ""
                                            val apellidoMaterno = medicoDocument.getString("Apellido Paterno") ?: ""

                                            val formattedFechaInicio = formatFecha(fechaInicio)

                                            tempTratamientos.add(
                                                Tratamiento(
                                                    medico = "$nombreMedico $apellidoMaterno",
                                                    fechaAsignacion = formattedFechaInicio,
                                                    diagnostico = diagnostico,
                                                    sintomas = sintomas,
                                                    recomendaciones = recomendaciones
                                                )
                                            )
                                        } else {
                                            println("No se encontró el documento del médico con ID: ${medicoRef.id}")
                                        }
                                    } else {
                                        println("Error al obtener el documento del médico: ${medicoTask.exception}")
                                    }

                                    remainingTasks--
                                    if (remainingTasks == 0) {
                                        // Ordenar tratamientos por fecha de asignación
                                        tratamientos.clear()
                                        tratamientos.addAll(tempTratamientos.sortedBy {
                                            parseFecha(it.fechaAsignacion)
                                        })
                                        notifyDataSetChanged()
                                        callback()
                                    }
                                }
                            } else {
                                println("El ID del médico es nulo para el tratamiento con ID: ${document.id}")
                                remainingTasks--
                                if (remainingTasks == 0) {
                                    // Ordenar tratamientos por fecha de asignación
                                    tratamientos.clear()
                                    tratamientos.addAll(tempTratamientos.sortedBy {
                                        parseFecha(it.fechaAsignacion)
                                    })
                                    notifyDataSetChanged()
                                    callback()
                                }
                            }
                        }
                    } else {
                        println("No se encontraron documentos de tratamientos.")
                        callback()
                    }
                } else {
                    println("Error al obtener los documentos: ${task.exception}")
                    callback()
                }
            }
        } else {
            println("Paciente ID no encontrado en SharedPreferences.")
            callback()
        }
    }

    private fun formatFecha(fecha: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = inputFormat.parse(fecha)
            val outputFormat = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault())
            if (date != null) {
                outputFormat.format(date)
            } else {
                fecha
            }
        } catch (e: Exception) {
            fecha
        }
    }

    private fun parseFecha(fecha: String): Date? {
        return try {
            val format = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.getDefault())
            format.parse(fecha)
        } catch (e: Exception) {
            null
        }
    }
}