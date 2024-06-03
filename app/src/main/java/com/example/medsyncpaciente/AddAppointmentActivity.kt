package com.example.medsyncpaciente

import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class AddAppointmentActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var medicoSpinner: Spinner
    private lateinit var diaSpinner: Spinner
    private lateinit var horaSpinner: Spinner
    private lateinit var fechaDisponible: TextView
    private lateinit var horaDisponible: TextView
    private lateinit var diaDisponible: TextView

    // Firebase Firestore instance
    private lateinit var db: FirebaseFirestore

    // Horarios disponibles
    private val dias = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
    private val horas = listOf("9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM", "2:00 PM")

    private val medicoIdMap =
        mutableMapOf<String, String>() // Mapa para almacenar nombres y IDs de médicos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_appointment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_agendarCitas)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        medicoSpinner = findViewById(R.id.medico_spinner)
        diaSpinner = findViewById(R.id.dia_spinner)
        horaSpinner = findViewById(R.id.hora_spinner)
        fechaDisponible = findViewById(R.id.fechadisponible_tv)
        horaDisponible = findViewById(R.id.horaDisponible_tv)
        diaDisponible = findViewById(R.id.diaDisponible_tv)

        toolbar.title = ""
        toolbarTitle.text = "Agendar Cita"
        setSupportActionBar(toolbar)

        // Initialize Firebase Firestore
        db = Firebase.firestore

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        // Configurar el adapter para el spinner de días
        val diaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dias)
        diaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        diaSpinner.adapter = diaAdapter

        // Configurar el adapter para el spinner de horas
        val horaAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, horas)
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        horaSpinner.adapter = horaAdapter

        // Configurar el adapter para el spinner de médicos
        val medicoAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        medicoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        medicoSpinner.adapter = medicoAdapter

        // Obtener los nombres completos de los médicos desde Firebase Firestore
        db.collection("Medico")
            .get()
            .addOnSuccessListener { result ->
                val medicos = mutableListOf<String>()
                for (document in result) {
                    val nombre = document.getString("Nombre") ?: ""
                    val apellidoPaterno = document.getString("Apellido Paterno") ?: ""
                    val apellidoMaterno = document.getString("Apellido Materno") ?: ""
                    val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()
                    if (nombreCompleto.isNotEmpty()) {
                        medicos.add(nombreCompleto)
                        medicoIdMap[nombreCompleto] = document.id // Guardar el ID del médico
                    }
                }
                medicoAdapter.clear()
                medicoAdapter.addAll(medicos)
                medicoAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
                exception.printStackTrace()
            }

        // Listeners para actualizar la próxima fecha disponible cuando se seleccione un médico, un día o una hora
        val updateNextDateListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                updateNextAvailableDate()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        medicoSpinner.onItemSelectedListener = updateNextDateListener
        diaSpinner.onItemSelectedListener = updateNextDateListener
        horaSpinner.onItemSelectedListener = updateNextDateListener
    }

    private fun updateNextAvailableDate() {
        val selectedMedico = medicoSpinner.selectedItem?.toString() ?: return
        val selectedDia = diaSpinner.selectedItem?.toString() ?: return
        val selectedHora = horaSpinner.selectedItem?.toString() ?: return

        Log.d(
            "UpdateNextDate",
            "Médico seleccionado: $selectedMedico, Día seleccionado: $selectedDia, Hora seleccionada: $selectedHora"
        )

        val medicoId =
            medicoIdMap[selectedMedico] ?: return // Obtener el ID del médico seleccionado

        // Consultar la colección "Citas" para obtener las próximas fechas disponibles del médico
        db.collection("Citas")
            .whereEqualTo("MedicoID", "/Medico/$medicoId")
            .get()
            .addOnSuccessListener { citasResult ->
                val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formatoDia = SimpleDateFormat("EEEE", Locale("es"))
                val formatoHora = SimpleDateFormat("h:mm a", Locale.getDefault())

                val citas = citasResult.documents.mapNotNull {
                    it.getString("Fecha_Hora")?.let { fecha -> formato.parse(fecha) }
                }
                Log.d("CitasObtenidas", "Citas obtenidas: ${citas.joinToString()}")

                // Verificar si el médico tiene citas agendadas en la fecha y hora seleccionadas
                var diaSeleccionado: Date? = null
                for (i in 0..6) {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_YEAR, i)
                    if (formatoDia.format(calendar.time) == selectedDia) {
                        diaSeleccionado = calendar.time
                        break
                    }
                }

                if (diaSeleccionado != null) {
                    val horasDisponibles = mutableListOf<Date>()
                    val calendar = Calendar.getInstance().apply { time = diaSeleccionado }
                    for (hora in horas) {
                        val horaPartes = hora.split(":")
                        val ampm = hora.split(" ")[1]
                        val horas24 =
                            if (ampm == "PM" && horaPartes[0] != "12") horaPartes[0].toInt() + 12 else horaPartes[0].toInt()
                        calendar.set(Calendar.HOUR_OF_DAY, horas24)
                        calendar.set(Calendar.MINUTE, horaPartes[1].split(" ")[0].toInt())
                        horasDisponibles.add(calendar.time)
                    }
                    val proximaFecha = horasDisponibles.firstOrNull { fecha ->
                        citas.none { cita -> cita == fecha }
                    }

                    if (proximaFecha != null) {
                        fechaDisponible.text = formato.format(proximaFecha)
                        horaDisponible.text = formatoHora.format(proximaFecha)
                        diaDisponible.text = selectedDia
                        Log.d(
                            "FechaDisponible",
                            "Próxima fecha disponible: ${formato.format(proximaFecha)}"
                        )
                    } else {
                        fechaDisponible.text = "No disponible"
                        horaDisponible.text = ""
                        diaDisponible.text = ""
                        Log.d("FechaDisponible", "No hay fechas disponibles")
                    }
                } else {
                    fechaDisponible.text = "No disponible"
                    horaDisponible.text = ""
                    diaDisponible.text = ""
                    Log.d("FechaDisponible", "Día seleccionado no válido")
                }
            }
            .addOnFailureListener { exception ->
                // Manejo de errores
                exception.printStackTrace()
                fechaDisponible.text = "Error al obtener disponibilidad"
                horaDisponible.text = ""
                diaDisponible.text = ""
                Log.e("FirestoreError", "Error al obtener las citas", exception)
            }
    }
}
