// AdaptadorCumplimiento.kt
package com.example.medsyncpaciente.Adapters

import AdaptadorTomas
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DetalleCumplimiento
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.FirebaseFirestore

class AdaptadorCumplimiento(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorCumplimiento.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    var medicamentosAsignados = mutableListOf<String>()
    var frecuenciaMedicamentos = mutableListOf<Int>()
    var cantiadadMedicamentos = mutableListOf<Int>()
    var medicamentosConFrecuencia = mutableMapOf<String, Pair<Int, Int>>()

    private val diccionarioFrecuencia = mapOf(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_cumplimiento_ejemplo, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicamento = medicamentosAsignados[position]
        val frecuencia = medicamentosConFrecuencia[medicamento]?.first ?: 0
        val cantidad = medicamentosConFrecuencia[medicamento]?.second ?: 0
        val frec = diccionarioFrecuencia[frecuencia]

        holder.medicamentoText.text = medicamento
        holder.frecuenciaText.text = frec

        // Crear la lista de tomas para este medicamento
        val tomas = List(frecuencia) { it + 1 }
        val adaptadorTomas = AdaptadorTomas(context, tomas, medicamento, sharedPreferences)
        holder.recyclerViewTomas.adapter = adaptadorTomas
        println("Se mando a llamar al adaptador")
        holder.recyclerViewTomas.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        holder.card.setOnClickListener {
            val intent = Intent(context, DetalleCumplimiento::class.java).apply {
                putExtra("medicamento", medicamento)
                putExtra("cantidad", cantidad)
                putExtra("dias", cantidad / frecuencia)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return medicamentosAsignados.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicamentoText: TextView = itemView.findViewById(R.id.medicamento_tv)
        var frecuenciaText: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var card: CardView = itemView.findViewById(R.id.cumplimiento_cardview)
        var recyclerViewTomas: RecyclerView = itemView.findViewById(R.id.recycler_Tomas)
    }

    fun cargarMedicamentos(callback: () -> Unit) {
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId != null) {
            val medicamentosRef = db.collection("Paciente").document(pacienteId).collection("Medicamentos")

            medicamentosRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        for (document in documents) {
                            val medicamento = document.id
                            val frecuencia = document.getLong("Frecuencia")?.toInt()
                            val cantidad = document.getLong("Cantidad")?.toInt()

                            if (frecuencia != null && cantidad != null) {
                                medicamentosAsignados.add(medicamento)
                                frecuenciaMedicamentos.add(frecuencia)
                                cantiadadMedicamentos.add(cantidad)
                                medicamentosConFrecuencia[medicamento] = Pair(frecuencia, cantidad)
                            } else {
                                println("El campo 'Frecuencia' o 'Cantidad' no está presente en el documento: $medicamento")
                            }
                        }
                        callback()
                    } else {
                        println("No se encontraron documentos de medicamentos.")
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
}
