package com.example.medsyncpaciente.fragments.registromediciones

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.medsyncpaciente.MeassurementFragment
import com.example.medsyncpaciente.R
import java.text.SimpleDateFormat
import java.util.Date

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OxigenoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OxigenoFragment : Fragment(), MeassurementFragment {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var valorMedicion: EditText
    private lateinit var hora: TextView

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_oxigeno, container, false)
        valorMedicion = view.findViewById<EditText>(R.id.medicion_et)
        hora = view.findViewById<TextView>(R.id.hora_tv)

        // Obtener la hora actual y formatearla
        val horaActual = obtenerHoraActual()
        hora.text = horaActual

        return view
    }

    // Función para obtener la hora actual en el formato deseado
    private fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("hh:mm a") // Formato de hora (por ejemplo: 11:00 a.m.)
        val fechaActual = Date()
        return formatoHora.format(fechaActual)
    }

    // Método para obtener las mediciones de la presión arterial
    override fun getMedicionesContent(): List<String> {
        return listOf(valorMedicion.text.toString())
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OxigenoFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OxigenoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}