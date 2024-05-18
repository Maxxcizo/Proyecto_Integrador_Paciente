package com.example.medsyncpaciente.Adapters

import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

class AdaptadorRegistroSintomas(private val context: Context, private val sharedPreferences: SharedPreferences) : RecyclerView.Adapter<AdaptadorRegistroSintomas.ViewHolder>() {

    // Instancia de la base de datos
    private val db = FirebaseFirestore.getInstance()

    // Variable para almacenar los síntomas asignados al paciente
    var sintomasAsignados = mutableListOf<String>()

    // Variable para almacenar los síntomas seleccionados
    var selectedSintomas = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_registro_sintomas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("Síntomas Asignados onBindViewHolder: $sintomasAsignados")

        val syntoms = sintomasAsignados[position]

        holder.sintomastext.text = syntoms
    }

    override fun getItemCount(): Int {
        return sintomasAsignados.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sintomastext: TextView = itemView.findViewById(R.id.sintoma_tv)
        var check: CheckBox = itemView.findViewById(R.id.checkbox)

        init {
            check.setOnCheckedChangeListener { _, isChecked ->
                val symptom = sintomastext.text.toString()
                if (isChecked) {
                    if (!selectedSintomas.contains(symptom)) {
                        selectedSintomas.add(symptom)
                    }
                } else {
                    selectedSintomas.remove(symptom)
                }
            }
        }
    }

    fun cargarMediciones(callback: () -> Unit) {
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId == null) {
            Log.w(TAG, "Paciente ID no encontrado en SharedPreferences")
            return
        }

        val medicionRef = db.collection("Paciente").document(pacienteId).collection("Sintomas")

        medicionRef.get()
            .addOnSuccessListener { querySnapshot ->
                sintomasAsignados.clear()
                for (document in querySnapshot) {
                    val medicionId = document.id
                    sintomasAsignados.add(medicionId)
                }
                println("Síntomas Asignados: $sintomasAsignados")
                notifyDataSetChanged()
                callback()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                callback()
            }
    }

    fun guardarSintomasSeleccionados() {
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId == null) {
            Log.w(TAG, "Paciente ID no encontrado en SharedPreferences")
            return
        }

        val fechaActual = Date()
        val formatoFechaHora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val fechaHoraFormateada = formatoFechaHora.format(fechaActual)

        for(sel in selectedSintomas){

            val sintomasSeleccionadosMap = hashMapOf(
                "fecha" to fechaHoraFormateada
            )

            db.collection("Paciente").document(pacienteId).collection("Sintomas").document(sel).collection("Registro de Sintoma")
                .add(sintomasSeleccionadosMap)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    Toast.makeText(context, "Síntomas guardados exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                    Toast.makeText(context, "Error al registrar síntomas", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
