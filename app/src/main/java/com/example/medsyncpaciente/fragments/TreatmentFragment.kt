package com.example.medsyncpaciente.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorTratamientos
import com.example.medsyncpaciente.DataClases.Tratamiento
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.FirebaseFirestore

private const val TAG = "TreatmentFragment"

class TreatmentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptadorTratamientos: AdaptadorTratamientos
    private lateinit var sharedPreferences: SharedPreferences
    private var pacienteID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_treatment, container, false)
        recyclerView = view.findViewById(R.id.recyclerView_Treatment)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        pacienteID = sharedPreferences.getString("pacienteId", null)

        adaptadorTratamientos = AdaptadorTratamientos(requireContext(), sharedPreferences)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adaptadorTratamientos
        }

        loadTratamientos()
    }

    private fun loadTratamientos() {
        if (pacienteID != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("Paciente").document(pacienteID!!).collection("Tratamientos")
                .get()
                .addOnSuccessListener { result ->
                    val tratamientos = mutableListOf<Tratamiento>()
                    for (document in result) {
                        val tratamientoRef = document.getDocumentReference("TratamientoID") ?: continue
                        tratamientoRef.get()
                            .addOnSuccessListener { citaDocument ->
                                val fechaInicio = citaDocument.getString("FechaInicio") ?: ""
                                val fechaFin = citaDocument.getString("FechaFin") ?: ""
                                val diagnostico = citaDocument.getString("Diagnostico") ?: ""
                                val sintomas = citaDocument.get("Sintomas") as? List<String> ?: emptyList()
                                val recomendaciones = citaDocument.getString("Recomendaciones") ?: ""
                                val medicoRef = citaDocument.getDocumentReference("MedicoId") ?: return@addOnSuccessListener

                                medicoRef.get()
                                    .addOnSuccessListener { medicoDocument ->
                                        val nombre = medicoDocument.getString("Nombre(s)") ?: ""
                                        val apellidoPaterno = medicoDocument.getString("Apellido Paterno") ?: ""
                                        val apellidoMaterno = medicoDocument.getString("Apellido Materno") ?: ""
                                        val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()

                                        tratamientos.add(
                                            Tratamiento(
                                                document.id,
                                                nombreCompleto,
                                                fechaInicio,
                                                fechaFin,
                                                diagnostico,
                                                sintomas,
                                                recomendaciones,
                                                tratamientoRef
                                            )
                                        )

                                        // Ordenar tratamientos por fecha de inicio
                                        tratamientos.sortBy { it.fechaInicio }
                                        adaptadorTratamientos.actualizarLista(tratamientos)
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(TAG, "Error al obtener el documento del médico", exception)
                                        // Manejar error al obtener el documento del médico
                                        showError("Error al obtener el médico")
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "Error al obtener el documento de tratamiento", exception)
                                // Manejar error al obtener el documento de tratamiento
                                showError("Error al obtener el tratamiento")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error al obtener los documentos de tratamientos", exception)
                    // Manejar error al obtener los documentos de tratamientos
                    showError("Error al obtener los tratamientos")
                }
        } else {
            Log.e(TAG, "Paciente ID no encontrado en SharedPreferences")
            // Manejar caso donde pacienteID es null
            showError("Paciente ID no encontrado")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        // Aquí puedes implementar cualquier otra lógica para manejar el error, como mostrar un mensaje en la UI
    }
}
