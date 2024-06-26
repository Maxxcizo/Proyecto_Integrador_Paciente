package com.example.medsyncpaciente

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddAppointmentActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView

    private lateinit var db: FirebaseFirestore
    private lateinit var medicoSpinner: Spinner
    private lateinit var horaSpinner: Spinner
    private lateinit var diaSpinner: Spinner
    private lateinit var diaAdapter: ArrayAdapter<CharSequence>
    private lateinit var medicoAdapter: ArrayAdapter<String>
    private lateinit var horaAdapter: ArrayAdapter<CharSequence>
    private lateinit var fechaDisponible: TextView
    private lateinit var diaDisponible: TextView
    private lateinit var horaDisponible: TextView
    private lateinit var buscarSiguienteButton: Button
    private lateinit var confirmarButton: Button

    @RequiresApi(Build.VERSION_CODES.O)
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val medicoReferenceMap = mutableMapOf<String, DocumentReference>()
    private var weekOffset = 0
    private var selectedDateTime: LocalDateTime? = null
    private var selectedMedico: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_appointment)

        db = FirebaseFirestore.getInstance()
        fechaDisponible = findViewById(R.id.fechadisponible_tv)
        diaDisponible = findViewById(R.id.diaDisponible_tv)
        horaDisponible = findViewById(R.id.horaDisponible_tv)
        buscarSiguienteButton = findViewById(R.id.buscarSiguiente_btn)
        confirmarButton = findViewById(R.id.confirmar_btn) // Button to confirm appointment
        toolbar = findViewById<Toolbar>(R.id.toolbar_agendarCitas)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)

        toolbar.title = ""
        toolbarTitle.text = "Agendar Cita"
        setSupportActionBar(toolbar)

        // Inicializar spinners y adapters
        initSpinnersAndAdapters()

        // Cargar médicos desde Firestore
        loadMedicos()

        // Configurar listeners para los spinners
        configureSpinners()

        backIcon.setOnClickListener {
            onBackPressed()
        }

        buscarSiguienteButton.setOnClickListener {
            weekOffset++
            updateNextAvailableDate()
        }

        confirmarButton.setOnClickListener {
            confirmAppointment()
        }

        // Obtener datos de la cita cancelada si existen
        val citaFecha = intent.getStringExtra("CITA_FECHA")
        val medicoID = intent.getStringExtra("MEDICO_ID")
        if (citaFecha != null && medicoID != null) {
            prefillAppointmentDetails(citaFecha, medicoID)
        }
    }

    private fun initSpinnersAndAdapters() {
        // Spinner de días
        diaSpinner = findViewById(R.id.dia_spinner)
        diaAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.dias_semana,
            android.R.layout.simple_spinner_item
        )
        diaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        diaSpinner.adapter = diaAdapter

        // Spinner de horas
        horaSpinner = findViewById(R.id.hora_spinner)
        horaAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.horas_citas,
            android.R.layout.simple_spinner_item
        )
        horaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        horaSpinner.adapter = horaAdapter

        // Spinner de médicos
        medicoSpinner = findViewById(R.id.medico_spinner)
        medicoAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mutableListOf())
        medicoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        medicoSpinner.adapter = medicoAdapter
    }

    private fun configureSpinners() {
        diaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                weekOffset = 0
                updateNextAvailableDate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

        horaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                weekOffset = 0
                updateNextAvailableDate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }

        medicoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                weekOffset = 0
                updateNextAvailableDate()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No hacer nada
            }
        }
    }

    private fun loadMedicos() {
        db.collection("Medico")
            .get()
            .addOnSuccessListener { result ->
                val medicos = mutableListOf<String>()
                for (document in result) {
                    val nombre = document.getString("Nombre(s)") ?: ""
                    val apellidoPaterno = document.getString("Apellido Paterno") ?: ""
                    val apellidoMaterno = document.getString("Apellido Materno") ?: ""
                    val nombreCompleto = "$nombre $apellidoPaterno $apellidoMaterno".trim()
                    if (nombreCompleto.isNotEmpty()) {
                        medicos.add(nombreCompleto)
                        medicoReferenceMap[nombreCompleto] = document.reference
                    }
                }
                medicoAdapter.clear()
                medicoAdapter.addAll(medicos)
                medicoAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error al cargar médicos", exception)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateNextAvailableDate() {
        val selectedDay = diaSpinner.selectedItem?.toString() ?: ""
        val selectedHour = horaSpinner.selectedItem?.toString() ?: ""

        // Check if selectedDay or selectedHour is empty
        if (selectedDay.isEmpty() || selectedHour.isEmpty()) {
            println("No se ha seleccionado un día u hora válidos.")
            return
        }

        // Obtener la fecha y hora seleccionadas
        val nextClosestDate = getNextClosestDate(selectedDay, selectedHour, weekOffset)

        // Convertir la fecha y hora seleccionadas a LocalDateTime
        selectedDateTime = LocalDateTime.parse(nextClosestDate, dateTimeFormatter)

        // Obtener la referencia del médico seleccionado
        selectedMedico = medicoSpinner.selectedItem?.toString() ?: ""
        val medicoRef = medicoReferenceMap[selectedMedico]

        if (medicoRef != null) {
            // Consultar las citas filtrando por el medicoRef seleccionado
            db.collection("Citas")
                .whereEqualTo("MedicoID", medicoRef)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    var citaDisponible = true

                    for (document in querySnapshot.documents) {
                        val fechaHora = document.getString("Fecha_Hora") ?: ""

                        // Convertir la fecha y hora de la cita a LocalDateTime
                        val citaDateTime = LocalDateTime.parse(fechaHora, dateTimeFormatter)

                        // Verificar si hay una cita en la misma fecha y hora
                        if (citaDateTime == selectedDateTime) {
                            citaDisponible = false
                            println("Ya existe una cita para la fecha y hora seleccionadas.")
                            break
                        }
                    }

                    if (citaDisponible) {
                        println("No hay citas registradas para la fecha y hora seleccionadas. Puedes agregar la cita.")

                        fechaDisponible.text = nextClosestDate
                        diaDisponible.text = diaSpinner.selectedItem.toString()
                        horaDisponible.text = horaSpinner.selectedItem.toString()

                    } else {
                        println("No puedes agregar la cita porque ya existe una cita para esa fecha y hora.")
                        weekOffset++
                        updateNextAvailableDate() // Buscar la siguiente semana
                    }
                }
                .addOnFailureListener { exception ->
                    // Manejar errores
                    println("Error al consultar citas: ${exception.message}")
                }
        } else {
            println("No se encontró referencia para el médico seleccionado: $selectedMedico")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNextClosestDate(selectedDay: String, selectedHour: String, weeksToAdd: Int): String {
        val today = LocalDateTime.now()
        val selectedDayOfWeek = when (selectedDay) {
            "lunes" -> DayOfWeek.MONDAY
            "martes" -> DayOfWeek.TUESDAY
            "miércoles" -> DayOfWeek.WEDNESDAY
            "jueves" -> DayOfWeek.THURSDAY
            "viernes" -> DayOfWeek.FRIDAY
            "sábado" -> DayOfWeek.SATURDAY
            "domingo" -> DayOfWeek.SUNDAY
            else -> throw IllegalArgumentException("Día inválido")
        }

        var date = today.with(selectedDayOfWeek).plusWeeks(weeksToAdd.toLong())
        if (date.isBefore(today) || (date.isEqual(today) && LocalTime.now() >= LocalTime.parse(selectedHour))) {
            date = date.plusWeeks(1)
        }

        val nextClosestDate = LocalDateTime.of(date.toLocalDate(), LocalTime.parse(selectedHour))

        return nextClosestDate.format(dateTimeFormatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmAppointment() {
        val selectedDateTime = selectedDateTime ?: return
        val selectedMedico = selectedMedico ?: return
        val medicoRef = medicoReferenceMap[selectedMedico] ?: return

        val pacienteID = "F0JBxiZ6OVkS5gAcpWtM" // Reemplazar con el ID real del paciente
        val nuevaCita = hashMapOf(
            "Fecha_Hora" to selectedDateTime.format(dateTimeFormatter),
            "MedicoID" to medicoRef,
            "PacienteID" to db.document("/Paciente/$pacienteID")
        )

        db.collection("Citas")
            .add(nuevaCita)
            .addOnSuccessListener { documentReference ->
                val citaID = documentReference.id
                val citaRef = db.document("Citas/$citaID")

                // Agregar la cita a la subcolección de citas del paciente
                db.document("/Paciente/$pacienteID")
                    .collection("Citas")
                    .add(mapOf("CiataID" to citaRef))
                    .addOnSuccessListener {
                        println("Cita agregada a la subcolección del paciente.")
                    }
                    .addOnFailureListener { exception ->
                        println("Error al agregar cita a la subcolección del paciente: ${exception.message}")
                    }

                // Agregar la cita a la subcolección de citas del médico
                medicoRef.collection("Citas")
                    .add(mapOf("CitaID" to citaRef))
                    .addOnSuccessListener {
                        println("Cita agregada a la subcolección del médico.")
                    }
                    .addOnFailureListener { exception ->
                        println("Error al agregar cita a la subcolección del médico: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                println("Error al agregar cita: ${exception.message}")
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prefillAppointmentDetails(citaFecha: String, medicoID: String) {

        println("medicoID: $medicoID")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale("es", "ES")) // Asegúrate de usar el Locale correcto
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        try {
            val date = dateFormat.parse(citaFecha)
            val dia = date?.let { dayFormat.format(it) }
            val hora = date?.let { timeFormat.format(it) }
            println("dia: $dia")
            println("hora: $hora")

            val diaIndex = diaAdapter.getPosition(dia)
            val horaIndex = horaAdapter.getPosition(hora)

            println("dia index: $diaIndex")
            println("hora index: $horaIndex")

            diaSpinner.setSelection(diaIndex)
            horaSpinner.setSelection(horaIndex)

        } catch (e: Exception) {
            println("Dia de la cita no obtenido")
            println("Hora de la cita no obtenida")
        }

        for ((key, value) in medicoReferenceMap) {
            println("$key -> ${value.id}")
        }

        // Buscar y establecer el médico en el spinner
        for (i in 0 until medicoAdapter.count) {
            val medico = medicoAdapter.getItem(i)
            println("Comparacion: ${medicoReferenceMap[medico]} == $medicoID")
            if (medicoReferenceMap[medico]?.id == medicoID) {
                medicoSpinner.setSelection(i)
                break
            }
        }

        // Actualizar la fecha disponible
        updateNextAvailableDate()
    }

    companion object {
        private const val TAG = "AddAppointmentActivity"
    }


}
