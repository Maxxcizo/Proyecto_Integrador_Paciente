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

class HomeActivity : AppCompatActivity() {
    private lateinit var todayFragment: TodayFragment
    private lateinit var progressFragment: ProgressFragment
    private lateinit var treatmentFragment: TreatmentFragment
    private lateinit var measurementsFragment: MeasurementFragment
    private lateinit var medicamentosFragment: MedicamentosFragment
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbarTitle: TextView
    private lateinit var toolbar: Toolbar

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

        val bundle = intent.extras
        val email = bundle?.getString("email")
        val password = bundle?.getString("password")



        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("password", password)
        prefs.apply()

        toolbar.title = ""
        setSupportActionBar(toolbar)

        makeCurrentFragment(todayFragment)

        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_today -> {
                    if (getCurrentFragment() !is TodayFragment) {
                        makeCurrentFragment(todayFragment)
                        toolbarTitle.text = "Today"
                        Log.d("BottomNavigation", "Today Fragment Selected")
                    }
                }
                R.id.menu_progress -> {
                    if (getCurrentFragment() !is ProgressFragment) {
                        makeCurrentFragment(progressFragment)
                        toolbarTitle.text = "Progress"
                        Log.d("BottomNavigation", "Progress Fragment Selected")
                    }
                }
                R.id.menu_treatment -> {
                    if (getCurrentFragment() !is TreatmentFragment) {
                        makeCurrentFragment(treatmentFragment)
                        toolbarTitle.text = "Treatment"
                        Log.d("BottomNavigation", "Treatment Fragment Selected")
                    }
                }
                R.id.menu_measurements -> {
                    if (getCurrentFragment() !is MeasurementFragment) {
                        makeCurrentFragment(measurementsFragment)
                        toolbarTitle.text = "Measurements"
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
                toolbar.menu.findItem(R.id.menu_export)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
            is ProgressFragment -> {
                toolbar.menu.findItem(R.id.menu_notification)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_export)?.isVisible = true
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
            is TreatmentFragment, is MeasurementFragment, is MedicamentosFragment -> {
                toolbar.menu.findItem(R.id.menu_notification)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_export)?.isVisible = false
                toolbar.menu.findItem(R.id.menu_account)?.isVisible = true
                toolbar.menu.findItem(R.id.menu_citas)?.isVisible = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        toolbar.menu.findItem(R.id.menu_notification)?.isVisible = true
        toolbar.menu.findItem(R.id.menu_export)?.isVisible = false
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
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_export -> {
                startActivity(Intent(this, ExportActivity::class.java))
                Toast.makeText(this, "Export", Toast.LENGTH_SHORT).show()
                return true
            }
            R.id.menu_notification -> {
                startActivity(Intent(this, NotificationsActivity::class.java))
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
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
