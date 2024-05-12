package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroMedicionesActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class AdaptadorMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorMediciones.ViewHolder>() {

    private val diccionarioMediciones = mapOf(
        1 to "Presión Arterial",
        2 to "Glucosa en Sangre",
        3 to "Oxigenacion en Sangre",
        4 to "Frecuencia Cardiaca"
    )

    private val diccionarioFrecuencia = mapOf(
        1 to "1 vez al día",
        2 to "2 veces al día",
        3 to "3 veces al día",
        4 to "4 veces al día"
    )

    private val horario = arrayOf("8:00 a.m.", "9:00 a.m.", "10:00 a.m.", "11:00 a.m.")
    private val imageReference = arrayOf(R.drawable.ic_blood_pressure, R.drawable.ic_blood_glucose, R.drawable.ic_blood_oxygen, R.drawable.ic_heart_rate)

    private val db = FirebaseFirestore.getInstance()

    private var mediciones = mutableListOf<Int>()
    private var frecuencia = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = mediciones.getOrNull(position)
        val frec = frecuencia.getOrNull(position)
        if (med != null) {
            holder.mediciontext.text = diccionarioMediciones[med]
            holder.frecuenciatext.text = diccionarioFrecuencia.getOrDefault(frec, "")
            holder.horarioText.text = horario[position]
            holder.iconoImage.setImageResource(imageReference[position])
            holder.card.setOnClickListener {
                val intent = Intent(context, RegistroMedicionesActivity::class.java).apply {
                    putExtra("medicion", diccionarioMediciones[med])
                    putExtra("frecuencia", diccionarioFrecuencia.getOrDefault(frec, ""))
                    putExtra("hora", horario[position])
                }
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return mediciones.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mediciontext: TextView = itemView.findViewById(R.id.medicion_tv)
        var frecuenciatext: TextView = itemView.findViewById(R.id.frecuencia_tv)
        var horarioText: TextView = itemView.findViewById(R.id.horas_tv)
        var iconoImage: ImageView = itemView.findViewById(R.id.iconoMedicion)
        var card: CardView = itemView.findViewById(R.id.mediciones_cardview)
    }

    // Método para cargar las mediciones desde Firestore
    fun cargarMediciones() {
        val idsDocumentosMedicion = mutableListOf<String>()

        db.collection("Registro de Medicion")
            .whereEqualTo("Estado", "Asignado")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val medicionRef = document.get("MedicionID") as DocumentReference?
                    if (medicionRef != null) {
                        val medicionID = medicionRef.id
                        idsDocumentosMedicion.add(medicionID)
                    }
                }

                db.collection("Medicion")
                    .whereIn(FieldPath.documentId(), idsDocumentosMedicion)
                    .get()
                    .addOnSuccessListener { medicionDocuments ->
                        for (d in medicionDocuments) {
                            val tipo = d.getDouble("Tipo")
                            if (tipo != null && diccionarioMediciones.containsKey(tipo.toInt())) {
                                mediciones.add(tipo.toInt())
                            }
                        }
                        notifyDataSetChanged()
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "Error obteniendo documentos de Medicion: ", exception)
                    }

                // Limpiar la lista de frecuencia antes de agregar nuevos datos
                frecuencia.clear()

                db.collection("Frecuencia")
                    .whereIn("MedicionID", idsDocumentosMedicion)
                    .get()
                    .addOnSuccessListener { frecuenciaDocuments ->
                        Log.d("TAG", "Cantidad de documentos de frecuencia: ${frecuenciaDocuments.size()}")
                        for (f in frecuenciaDocuments) {
                            Log.d("TAG", "Documento de frecuencia: $f")
                            val medicionID = f.getDocumentReference("MedicionID")?.id
                            if (medicionID != null && idsDocumentosMedicion.contains(medicionID)) {
                                val frec = f.getLong("Frecuencia")
                                Log.d("TAG", "Frecuencia obtenida: $frec")
                                if (frec != null && diccionarioFrecuencia.containsKey(frec.toInt())) {
                                    frecuencia.add(frec.toInt())
                                }
                            }
                        }
                        notifyDataSetChanged()
                        Log.d("TAG", "frecuencias ,$frecuencia")
                    }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "Error obteniendo documentos de Frecuencia: ", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error obteniendo documentos de Registro de Medicion: ", exception)
            }
    }

}
