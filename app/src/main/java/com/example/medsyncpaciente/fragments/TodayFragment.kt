package com.example.medsyncpaciente.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorMedicina
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.FirebaseFirestore

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

data class Medicamento(
    val nombre: String,
    val cantidad: Int,
    val frecuencia: Int,
)

class TodayFragment : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdaptadorMedicina
    private lateinit var pacienteId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_today, container, false)
        recyclerView = view.findViewById(R.id.recyclerView_Medicine)

        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        pacienteId = sharedPreferences.getString("pacienteId", null).toString()

        adapter = AdaptadorMedicina(requireActivity(), mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        obtenerMedicamentosDesdeFuenteDeDatos()

        return view
    }

    private fun obtenerMedicamentosDesdeFuenteDeDatos() {
        val db = FirebaseFirestore.getInstance()
        val medicamentos = mutableListOf<Medicamento>()

        db.collection("Paciente").document(pacienteId).collection("Medicamentos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val nombre = document.id
                    val cantidad = document.getLong("Cantidad")?.toInt() ?: 0
                    val frecuencia = document.getLong("Frecuencia")?.toInt() ?: 0

                    val medicamento = Medicamento(nombre, cantidad, frecuencia)
                    medicamentos.add(medicamento)
                }

                adapter.medicamentos = medicamentos
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Manejar errores aquí
            }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TodayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
