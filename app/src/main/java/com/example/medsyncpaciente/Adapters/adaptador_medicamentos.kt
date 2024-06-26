package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.AgregarMedicamentoActivity
import com.example.medsyncpaciente.ConfirmarTomaActivity
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity
import com.google.firebase.firestore.FirebaseFirestore

// Adaptador para el RecyclerView que maneja la lista de medicamentos
class AdaptadorMedicamentos(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorMedicamentos.ViewHolder>() {

    // Instancia de la base de datos Firestore
    private val db = FirebaseFirestore.getInstance()

    // Listas para almacenar los medicamentos asignados, sus frecuencias y la cantidad restante
    var medicamentosAsignados = mutableListOf<String>()
    var frecuenciaMedicamentos = mutableListOf<Int>()
    var cantiadadMedicamentos = mutableListOf<Int>()

    // Diccionario para almacenar los medicamentos con su frecuencia y cantidad
    val medicamentosConFrecuencia = mutableMapOf<String, Pair<Int?, Int?>>()

    // Diccionario para relacionar la frecuencia con su descripción en texto
    private val diccionarioFrecuencia = mapOf<Int, String?>(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    // Crea y retorna un ViewHolder con la vista de cada ítem de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_medicamentos, parent, false)
        return ViewHolder(v)
    }

    // Vincula los datos a la vista en la posición especificada
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("Medicamentos asignados: $medicamentosConFrecuencia")

        // Obtener el medicamento y su frecuencia en la posición actual
        val med = medicamentosAsignados.getOrNull(position)
        val cant = cantiadadMedicamentos.getOrNull(position)
        val dias = if (cantiadadMedicamentos.getOrNull(position) != null && frecuenciaMedicamentos.getOrNull(position) != null) {
            cantiadadMedicamentos[position] / frecuenciaMedicamentos[position]
        } else {
            null
        }

        if (med != null && cant != null) {
            // Configurar los textos del ViewHolder
            holder.medicamentoText.text = med
            holder.diasText.text = "" + dias +" día(s) restante(s)"
            holder.cantidadText.text = "" + cant +" tableta(s) restante(s)"

            // Configurar el click listener del card
            holder.card.setOnClickListener {
                val intent = Intent(context, AgregarMedicamentoActivity::class.java).apply {
                    putExtra("medicamento", med)
                    println("Medicamentos asignados: $medicamentosAsignados")
                    putExtra("cantidad", cant)
                    println("Medicamento Restante: $cant")
                    println("dias: $dias")
                    putExtra("dias", dias)
                }
                context.startActivity(intent)
            }
        } else {
            // Mostrar un mensaje si no hay suficiente información
            Toast.makeText(
                context,
                "No hay suficiente información para el medicamento en la posición $position",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Retorna la cantidad de ítems en la lista
    override fun getItemCount(): Int {
        return medicamentosAsignados.size
    }

    // ViewHolder que contiene las vistas de cada ítem
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var medicamentoText: TextView = itemView.findViewById(R.id.medicamento_tv)
        var diasText: TextView = itemView.findViewById(R.id.dias_tv)
        var cantidadText: TextView = itemView.findViewById(R.id.cantidad_tv)
        var card: CardView = itemView.findViewById(R.id.medicamentos_cardview)
    }

    // Método para cargar los medicamentos desde Firestore
    fun cargarMedicamentos(callback: () -> Unit) {
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId != null) {
            // Referencia a la colección de medicamentos del paciente en Firestore
            val medicamentosRef = db.collection("Paciente").document(pacienteId).collection("Medicamentos")

            medicamentosRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (documents != null && !documents.isEmpty) {
                        val totalMedicamentos = documents.size()
                        var medicamentosCargados = 0 // Contador para verificar todos los medicamentos cargados

                        for (document in documents) {
                            val medicamento = document.id
                            val frecuencia = document.getLong("Frecuencia")
                            val cantidad = document.getLong("Cantidad")

                            if (frecuencia != null && cantidad != null) {
                                // Agregar los datos del medicamento a las listas
                                medicamentosAsignados.add(medicamento)
                                frecuenciaMedicamentos.add(frecuencia.toInt())
                                println("La frecuencia de $medicamento es: $frecuencia")
                                cantiadadMedicamentos.add(cantidad.toInt())
                                println("La cantidad de $medicamento es: $cantidad")

                                // Agregar los datos del medicamento al diccionario
                                medicamentosConFrecuencia[medicamento] = Pair(frecuencia.toInt(), cantidad.toInt())
                            } else {
                                println("El campo 'Frecuencia' o 'Cantidad' no está presente en el documento: $medicamento")
                            }

                            medicamentosCargados++
                            // Verificar si todos los medicamentos se han cargado
                            if (medicamentosCargados == totalMedicamentos) {
                                callback()
                            }
                        }
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

    fun actualizarMedicamentos() {
        // Limpiar las listas de medicamentos
        medicamentosAsignados.clear()
        frecuenciaMedicamentos.clear()
        cantiadadMedicamentos.clear()
        medicamentosConFrecuencia.clear()

        // Volver a cargar los medicamentos desde Firestore
        cargarMedicamentos {
            // Notificar al RecyclerView que los datos han cambiado
            notifyDataSetChanged()
        }
    }
}
