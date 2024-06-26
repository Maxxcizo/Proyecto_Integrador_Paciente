package com.example.medsyncpaciente

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.medsyncpaciente.fragments.MeasurementFragment
import com.example.medsyncpaciente.fragments.MedicamentosFragment
import com.example.medsyncpaciente.fragments.ProgressFragment
import com.example.medsyncpaciente.fragments.TodayFragment
import com.example.medsyncpaciente.fragments.TreatmentFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeActivity : AppCompatActivity() {
    private lateinit var todayFragment: TodayFragment
    private lateinit var progressFragment: ProgressFragment
    private lateinit var treatmentFragment: TreatmentFragment
    private lateinit var measurementsFragment: MeasurementFragment
    private lateinit var medicamentosFragment: MedicamentosFragment
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var bd: FirebaseFirestore
    private lateinit var listenerRegistration: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        todayFragment = TodayFragment()
        progressFragment = ProgressFragment()
        treatmentFragment = TreatmentFragment()
        measurementsFragment = MeasurementFragment()
        medicamentosFragment = MedicamentosFragment()
        bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        toolbarTitle = findViewById<TextView>(R.id.toolbar_title)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        bd = FirebaseFirestore.getInstance()

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val password = bundle?.getString("password")
        var pacienteId = ""

        // Si email o password son nulos o vacíos, manejar el flujo aquí
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            // Manejo de error o redirección a la pantalla de inicio de sesión
            Log.e("HomeActivity", "No se encontró email o password en Intent extras")
            // Aquí deberías manejar el caso donde no hay datos de usuario disponibles
            return
        }

        // Obtener el userUID de Firebase Authentication basado en el correo electrónico y contraseña
        val auth = FirebaseAuth.getInstance()
        auth.signInWithEmailAndPassword(email ?: "", password ?: "")
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, get user UID
                    val user = auth.currentUser
                    val userUID = user?.uid

                    //Obtener el id del médico
                    bd.collection("Paciente")
                        .whereEqualTo("Correo", email)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                pacienteId = document.id
                                Log.d("HomeActivity", "Se obtuvo el id del paciente: $pacienteId")

                                // Guardar datos en SharedPreferences
                                val prefs = getSharedPreferences(
                                    getString(R.string.prefs_file),
                                    Context.MODE_PRIVATE
                                ).edit()
                                prefs.putString("email", email)
                                prefs.putString("password", password)
                                prefs.putString("userUID", userUID)
                                prefs.putString("pacienteId", pacienteId)
                                prefs.apply()

                                // Establecer listener para las citas canceladas
                                val queryCitas = bd.collection("Citas").whereEqualTo("PacienteID", bd.document("/Paciente/$pacienteId"))

                                listenerRegistration = queryCitas.addSnapshotListener { snapshots, e ->
                                    if (e != null) {
                                        return@addSnapshotListener
                                    }

                                    val sharedPrefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
                                    val notifiedCitas = sharedPrefs.getStringSet("notifiedCitas", mutableSetOf()) ?: mutableSetOf()
                                    val editor = sharedPrefs.edit()

                                    for (doc in snapshots!!.documentChanges) {
                                        val status = doc.document.getBoolean("Cancelada") ?: false
                                        val documentId = doc.document.id

                                        if (status) {
                                            val reason = doc.document.getString("RazonCancelacion") ?: "No especificada"
                                            val medicoId = doc.document.getDocumentReference("MedicoID")?.id

                                            if (!notifiedCitas.contains(documentId)) {
                                                if (medicoId != null) {
                                                    bd.collection("Medico").document(medicoId).get()
                                                        .addOnSuccessListener { medicoDoc ->
                                                            val nombre = medicoDoc.getString("Nombre(s)") ?: ""
                                                            val apellidoPaterno = medicoDoc.getString("Apellido Paterno") ?: ""
                                                            val nombreCompleto = "$nombre $apellidoPaterno"
                                                            sendNotificationCitaCancelada(documentId, reason, nombreCompleto)
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e("HomeActivity", "Error al obtener el nombre del médico: ", e)
                                                        }
                                                } else {
                                                    sendNotificationCitaCancelada(documentId, reason, "Desconocido")
                                                }
                                                notifiedCitas.add(documentId)
                                                editor.putStringSet("notifiedCitas", notifiedCitas).apply()
                                            }
                                        } else {
                                            if (notifiedCitas.contains(documentId)) {
                                                notifiedCitas.remove(documentId)
                                                editor.putStringSet("notifiedCitas", notifiedCitas).apply()
                                            }
                                        }
                                    }
                                }

                                // Consulta para escuchar cambios en la colección Medicamentos bajo el paciente específico
                                val queryMedicamentos = bd.collection("Paciente/$pacienteId/Medicamentos")

                                // Agregar el SnapshotListener para la consulta
                                listenerRegistration = queryMedicamentos.addSnapshotListener { snapshots, e ->
                                    if (e != null) {
                                        // Manejar errores aquí
                                        return@addSnapshotListener
                                    }

                                    for (doc in snapshots!!.documentChanges) {
                                        if (doc.type == DocumentChange.Type.MODIFIED) {
                                            val cantidadRestante = doc.document.getLong("Cantidad") ?: 0
                                            val frecuenciaToma = doc.document.getLong("Frecuencia") ?: 1  // veces al día
                                            val dosis = doc.document.getLong("Dosis") ?: 1  // pastillas por toma

                                            // Calcular el total de dosis diarias
                                            val dosisDiarias = frecuenciaToma * dosis

                                            // Calcular los días restantes
                                            val diasRestantes = if (dosisDiarias > 0) cantidadRestante / dosisDiarias else 0

                                            val medicamento = doc.document.id

                                            // Aquí puedes realizar cualquier acción que necesites cuando la cantidad se modifique
                                            // Por ejemplo, actualizar la interfaz de usuario o enviar una notificación
                                            println("Cantidad de $medicamento modificada a $cantidadRestante. Días restantes: $diasRestantes")

                                            // Notificar si los días restantes son menores a 2 días
                                            if (diasRestantes < 2) {
                                                sendNotificationMedicamentoBajo(medicamento, cantidadRestante, diasRestantes)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Error al obtener el documento: $exception")
                        }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("HomeActivity", "Authentication failed.")
                }
            }

        val prefs2 = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val allEntries: Map<String, *> = prefs2.all

        for ((key, value) in allEntries) {
            Log.d("SharedPreferences", "$key: $value")
        }

        toolbar.title = ""
        setSupportActionBar(toolbar)

        makeCurrentFragment(todayFragment)

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_today -> {
                    if (getCurrentFragment() !is TodayFragment) {
                        makeCurrentFragment(todayFragment)
                        toolbarTitle.text = "Hoy"
                        Log.d("BottomNavigation", "Today Fragment Selected")
                    }
                }
                R.id.menu_progress -> {
                    if (getCurrentFragment() !is ProgressFragment) {
                        makeCurrentFragment(progressFragment)
                        toolbarTitle.text = "Progreso"
                        Log.d("BottomNavigation", "Progress Fragment Selected")
                    }
                }
                R.id.menu_treatment -> {
                    if (getCurrentFragment() !is TreatmentFragment) {
                        makeCurrentFragment(treatmentFragment)
                        toolbarTitle.text = "Tratamientos"
                        Log.d("BottomNavigation", "Treatment Fragment Selected")
                    }
                }
                R.id.menu_measurements -> {
                    if (getCurrentFragment() !is MeasurementFragment) {
                        makeCurrentFragment(measurementsFragment)
                        toolbarTitle.text = "Mediciones"
                        Log.d("BottomNavigation", "Measure Fragment Selected")
                    }
                }
                R.id.menu_medicamentos -> {
                    if (getCurrentFragment() !is MedicamentosFragment) {
                        makeCurrentFragment(medicamentosFragment)
                        toolbarTitle.text = "Medicamentos"
                        Log.d("BottomNavigation", "Medicamentos Fragment Selected")
                    }
                }
            }
            true
        }
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
        // Actualizar visibilidad del menú basado en el fragmento actual
        when (fragment) {
            is TodayFragment -> {
                toolbar.menu.findItem(R.id.menu_notification)?.isVisible = true
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
            is ProgressFragment -> {
                toolbar.menu.findItem(R.id.menu_notification)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
            is TreatmentFragment, is MeasurementFragment, is MedicamentosFragment -> {
                toolbar.menu.findItem(R.id.menu_notification)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = true
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        toolbar.menu.findItem(R.id.menu_notification)?.isVisible = true
        toolbar.menu.findItem(R.id.menu_account)?.isVisible = false
        toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
        return super.onCreateOptionsMenu(menu)
    }

    private fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fl_wrapper)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_account -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                Toast.makeText(this, "Ajustes", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_notification -> {
                startActivity(Intent(this, NotificationsActivity::class.java))
                Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_citas -> {
                startActivity(Intent(this, CitasActivity::class.java))
                Toast.makeText(this, "Citas", Toast.LENGTH_SHORT).show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Detener la escucha cuando la actividad se destruye
        if (::listenerRegistration.isInitialized) {
            listenerRegistration.remove()
        }
    }

    private fun sendNotificationCitaCancelada(documentId: String, reason: String, nombreMedico: String) {
        // Intent para reprogramar la cita
        val reprogramIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "REPROGRAM_CITA"
            putExtra("documentId", documentId)
        }
        val reprogramPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, reprogramIntent, PendingIntent.FLAG_IMMUTABLE)

        // Intent para rechazar la reprogramación
        val rejectIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "REJECT_REPROGRAM"
            putExtra("documentId", documentId)
        }
        val rejectPendingIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, rejectIntent, PendingIntent.FLAG_IMMUTABLE)

        // Construir la notificación
        val builder = NotificationCompat.Builder(this, "default_channel")
            .setSmallIcon(R.drawable.ic_notification) // Cambia por tu propio ícono de notificación
            .setContentTitle("Cita Cancelada")
            .setContentText("La cita con el Dr. $nombreMedico ha sido cancelada. Razón: $reason")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            // Añadir acciones (botones) a la notificación
            .addAction(R.drawable.check_icon, "Reprogramar", reprogramPendingIntent)
            .addAction(R.drawable.cross_icon, "Rechazar", rejectPendingIntent)

        // Crear un canal de notificación para versiones de Android Oreo y superiores
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default_channel",
                "Default Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal predeterminado"
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Mostrar la notificación
        with(NotificationManagerCompat.from(this)) {
            notify(documentId.hashCode(), builder.build()) // Usamos documentId.hashCode() como ID único para la notificación
        }
    }

    private fun sendNotificationMedicamentoBajo(medicamento: String, cantidadRestante: Long, diasRestantes: Long) {
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "low_medication_channel")
            .setSmallIcon(R.drawable.notification_alert) // Usa tu propio icono aquí
            .setContentTitle("Medicamento Bajo")
            .setContentText("El medicamento $medicamento tiene $cantidadRestante pastillas restantes y se agotará en $diasRestantes días.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("low_medication_channel", "Low Medication Channel", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Canal para notificaciones de medicamentos bajos"
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        with(NotificationManagerCompat.from(this)) {
            notify(medicamento.hashCode(), builder.build()) // Usamos medicamento.hashCode() como ID único para la notificación
        }
    }
}
