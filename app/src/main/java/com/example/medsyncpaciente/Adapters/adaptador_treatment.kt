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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Tratamiento(
    val tratamientoID: String,
    val medico: String,
    val fechaInicio: String,
    val fechaFin: String,
    val diagnostico: String,
    val sintomas: List<String>,
    val recomendaciones: String,
    val tratamientoRef: DocumentReference
)

class AdaptadorTratamientos(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorTratamientos.ViewHolder>() {

    private val tratamientos = mutableListOf<Tratamiento>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val pacienteID = sharedPreferences.getString("pacienteId", null)

    init {
        // Recuperar tratamientos desde Firestore
        val db = FirebaseFirestore.getInstance()

        if (pacienteID != null) {
            println("PacienteID: $pacienteID")
            db.collection("Paciente").document(pacienteID).collection("Tratamientos")
                .get()
                .addOnSuccessListener { result ->
                    println("Número de documentos recuperados: ${result.size()}")
                    for (document in result) {
                        val tratamientoRef = document.getDocumentReference("TratamientoID") ?: continue
                        tratamientoRef.get()
                            .addOnSuccessListener { citaDocument ->
                                println("Documento recuperado: ${document.id}")
                                val fechaInicio = citaDocument.getString("Fecha_Inicio") ?: ""
                                val fechaFin = citaDocument.getString("Fecha_Fin") ?: ""
                                val diagnostico = citaDocument.getString("Diagnostico") ?: ""
                                val sintomas = citaDocument.get("Sintomas") as? List<String> ?: emptyList()
                                val recomendaciones = citaDocument.getString("Recomendaciones") ?: ""
                                val medicoRef = citaDocument.getDocumentReference("MedicoID") ?: return@addOnSuccessListener

                                // Obtener el nombre del médico
                                medicoRef.get()
                                    .addOnSuccessListener { medicoDocument ->
                                        val nombre = medicoDocument.getString("Nombre") ?: ""
                                        val apellidoPaterno = medicoDocument.getString("Apellido Paterno") ?: ""
                                        val apellidoMaterno = medicoDocument.getString("Apellido Materno") ?: ""
                                        val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                        println("TratamientoRef: $tratamientoRef")
                                        println("FechaInicio: $fechaInicio, FechaFin: $fechaFin, Diagnostico: $diagnostico, Sintomas: $sintomas, Recomendaciones: $recomendaciones")
                                        println("MedicoRef: $medicoRef")
                                        println("Nombre del médico: $nombreCompleto")

                                        tratamientos.add(Tratamiento(
                                            document.id,
                                            nombreCompleto,
                                            fechaInicio,
                                            fechaFin,
                                            diagnostico,
                                            sintomas,
                                            recomendaciones,
                                            tratamientoRef
                                        ))
                                        // Ordenar la lista de tratamientos por fecha de inicio después de agregar un nuevo tratamiento
                                        tratamientos.sortBy { dateFormat.parse(it.fechaInicio) }
                                        notifyDataSetChanged() // Notificar al adaptador de los cambios
                                    }
                                    .addOnFailureListener { exception ->
                                        println("Error al obtener el documento del médico: $exception")
                                    }
                            }
                            .addOnFailureListener { exception ->
                                println("Error al obtener el documento de tratamiento: $exception")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejo de errores
                    println("Error al obtener los documentos de tratamientos: $exception")
                }
        } else {
            println("Paciente ID no encontrado en SharedPreferences.")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_treatment, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tratamiento = tratamientos[position]

        println("Binding tratamiento en posición $position: $tratamiento")

        holder.medicotext.text = "Asignado por "+tratamiento.medico
        holder.fechaInicioText.text = tratamiento.fechaInicio
        holder.diagnosticoText.text = tratamiento.diagnostico

        holder.card.setOnClickListener {
            // Acción a realizar cuando se haga clic en el elemento de la lista
            println("Tratamiento clickeado: $tratamiento")
            val intent = Intent(context, DetallesTratamiento::class.java)
            intent.putExtra("TRATAMIENTO_ID", tratamiento.tratamientoID)
            intent.putExtra("TRATAMIENTO_REF", tratamiento.tratamientoRef.path) // Pasar la referencia del tratamiento como una cadena
            intent.putExtra("TRATAMIENTO_FECHA_INICIO", tratamiento.fechaInicio)
            intent.putExtra("TRATAMIENTO_FECHA_FIN", tratamiento.fechaFin)
            intent.putExtra("DIAGNOSTICO", tratamiento.diagnostico)
            intent.putExtra("SINTOMAS", tratamiento.sintomas.toTypedArray()) // Pasar los síntomas como array
            intent.putExtra("RECOMENDACIONES", tratamiento.recomendaciones)
            intent.putExtra("PACIENTE_ID", pacienteID) // Pasar pacienteID a DetallesTratamiento
            intent.putExtra("NOMBRE_COMPLETO", tratamiento.medico)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return tratamientos.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicotext: TextView = itemView.findViewById(R.id.medico_tv)
        var fechaInicioText: TextView = itemView.findViewById(R.id.fecha_tv)
        var diagnosticoText: TextView = itemView.findViewById(R.id.diagnostico_tv)
        var card: CardView = itemView.findViewById(R.id.treatment_cardview)
    }
}
