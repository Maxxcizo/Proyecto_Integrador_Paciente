package com.example.medsyncpaciente.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorMediciones
import com.example.medsyncpaciente.GraficasMedicionesActivity
import com.example.medsyncpaciente.R
import com.example.medsyncpaciente.RegistroSintomasActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class MeasurementFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var botonFlotante: FloatingActionButton
    private lateinit var registrarButton: Button

    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: AdaptadorMediciones
    private val diccionarioMediciones = listOf("Presion Arterial", "Glucosa en sangre", "Oxigenacion en Sangre", "Frecuencia Cardiaca")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_measurement, container, false)

        sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        recyclerView = view.findViewById(R.id.recyclerView_Mediciones)
        botonFlotante = view.findViewById(R.id.fab)
        registrarButton = view.findViewById(R.id.registrarMediciones_btn)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = AdaptadorMediciones(requireContext())
        recyclerView.adapter = adapter

        cargarMediciones()

        botonFlotante.setOnClickListener {
            startActivity(Intent(requireContext(), GraficasMedicionesActivity::class.java))
        }

        registrarButton.setOnClickListener {
            startActivity(Intent(requireContext(), RegistroSintomasActivity::class.java))
        }

        return view
    }

    private fun cargarMediciones() {
        val pacienteId = sharedPreferences.getString("pacienteId", null)
        val medicionesAsignadas = mutableListOf<String>()
        val frecuenciaMedicionesAsignadas = mutableListOf<Int>()
        var loadedMediciones = 0

        for (medicion in diccionarioMediciones) {
            val frecuenciaCardiacaRef =
                db.collection("Paciente").document(pacienteId.toString()).collection("Mediciones")
                    .document(medicion)

            frecuenciaCardiacaRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            val frecuencia = document.getLong("Frecuencia")
                            medicionesAsignadas.add(medicion)
                            if (frecuencia != null) {
                                frecuenciaMedicionesAsignadas.add(frecuencia.toInt())
                            }
                        }
                    }
                    loadedMediciones++
                    if (loadedMediciones == diccionarioMediciones.size) {
                        adapter.setMediciones(medicionesAsignadas, frecuenciaMedicionesAsignadas)
                    }
                }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MeasurementFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
