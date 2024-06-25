package com.example.medsyncpaciente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
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
import com.google.firebase.firestore.FirebaseFirestore

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
}
