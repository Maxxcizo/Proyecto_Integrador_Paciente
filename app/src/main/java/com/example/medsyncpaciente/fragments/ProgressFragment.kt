// ProgressFragment.kt
package com.example.medsyncpaciente.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorCumplimiento
import com.example.medsyncpaciente.R

class ProgressFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)

        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)

        recyclerView = view.findViewById(R.id.recyclerView_Cumplimiento)
        val adapter = AdaptadorCumplimiento(requireActivity(), sharedPreferences)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter.cargarMedicamentos {
            // Una vez cargados los medicamentos, establecer el adaptador
            recyclerView.adapter = adapter
        }

        return view
    }
}
