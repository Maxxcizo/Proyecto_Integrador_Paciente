package com.example.medsyncpaciente

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.medsyncpaciente.fragments.registromediciones.FrecuenciaFragment
import com.example.medsyncpaciente.fragments.registromediciones.GlucosaFragment
import com.example.medsyncpaciente.fragments.registromediciones.OxigenoFragment
import com.example.medsyncpaciente.fragments.registromediciones.PresionFragment

class RegistroMedicionesActivity : AppCompatActivity() {
    private lateinit var presionFragment: PresionFragment
    private lateinit var glucosaFragment: GlucosaFragment
    private lateinit var oxigenoFragment: OxigenoFragment
    private lateinit var frecuenciaFragment: FrecuenciaFragment
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var registrarButton: Button
    private lateinit var posponerButton: Button
    private lateinit var confirmarButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_mediciones)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        presionFragment = PresionFragment()
        glucosaFragment = GlucosaFragment()
        oxigenoFragment = OxigenoFragment()
        frecuenciaFragment = FrecuenciaFragment()
        toolbar = findViewById<Toolbar>(R.id.toolbar_registroMediciones)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        registrarButton = findViewById(R.id.registrarMediciones_btn)
        posponerButton = findViewById(R.id.posponer_btn)
        confirmarButton = findViewById(R.id.confirmar_btn)

        val bundle = intent.extras
        val medicion = bundle?.getString("medicion")
        val frecuencia = bundle?.getString("frecuencia")
        val hora = bundle?.getString("hora")

        Toast.makeText(this, "medicion: $medicion", Toast.LENGTH_SHORT).show()

        when (medicion){
            "Presión Arterial" -> {
                makeCurrentFragment(presionFragment)
            }
            "Glucosa en Sangre" -> {
                makeCurrentFragment(glucosaFragment)
            }
            "Oxigenacion en Sangre" -> {
                makeCurrentFragment(oxigenoFragment)
            }
            "Frecuencia Cardiaca" -> {
                makeCurrentFragment(frecuenciaFragment)
            }
            else -> {
                Toast.makeText(this, "La opcion no es válida", Toast.LENGTH_SHORT).show()
            }
        }

        toolbar.title = ""
        toolbarTitle.text = "Registro de Mediciones"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun makeCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }

        registrarButton.setOnClickListener {
            startActivity(Intent(this, RegistroSintomasActivity::class.java))
        }

        confirmarButton.setOnClickListener {
            // Obtener el fragmento actual
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fl_wrapper)

            // Verificar si es un fragmento de MeasurementFragment
            when (currentFragment){
                is PresionFragment -> {
                    // Obtener el arreglo que contiene el texto de los 3 EditText
                    val medicionesContent = currentFragment.getMedicionesContent()

                    var algunEditTextVacio = false

                    // For para recorrer cada elemento del arreglo, en este caso la lista contiene el texto de los 3 EditText
                    for (medicion in medicionesContent) {
                        if (medicion.isNotEmpty()) {
                            // Mostrar un Toast con el contenido del EditText
                            Toast.makeText(this, "Contenido del EditText: $medicion", Toast.LENGTH_SHORT).show()
                        } else {
                            // Mostrar un Toast si el EditText está vacío
                            Toast.makeText(this, "El EditText está vacío", Toast.LENGTH_SHORT).show()
                            algunEditTextVacio = true
                            break
                        }
                    }

                    // Verificar si algún EditText está vacío
                    if (algunEditTextVacio) {
                        // Mostrar un Toast indicando que al menos un EditText está vacío
                        Toast.makeText(this, "Al menos un EditText está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast indicando que todos los EditText tienen contenido
                        Toast.makeText(this, "Todos los EditText tienen contenido", Toast.LENGTH_SHORT).show()

                        // Aquí puedes continuar con el resto de tu lógica
                    }

                }
                is GlucosaFragment -> {
                    val editTextContent = currentFragment.getEditTextContent()
                    var editTextVacio = false
                    if (editTextContent.isNotEmpty()) {
                        // Mostrar un Toast con el contenido del EditText
                        Toast.makeText(this, "Contenido del EditText: $editTextContent", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast si el EditText está vacío
                        Toast.makeText(this, "El EditText está vacío", Toast.LENGTH_SHORT).show()
                        editTextVacio = true
                    }

                    // Verificar si algún EditText está vacío
                    if (editTextVacio) {
                        // Mostrar un Toast indicando que al menos un EditText está vacío
                        Toast.makeText(this, "Al menos un EditText está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast indicando que todos los EditText tienen contenido
                        Toast.makeText(this, "Todos los EditText tienen contenido", Toast.LENGTH_SHORT).show()

                        // Aquí puedes continuar con el resto de tu lógica
                    }
                }
                is OxigenoFragment -> {
                    val editTextContent = currentFragment.getEditTextContent()
                    var editTextVacio = false
                    if (editTextContent.isNotEmpty()) {
                        // Mostrar un Toast con el contenido del EditText
                        Toast.makeText(this, "Contenido del EditText: $editTextContent", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast si el EditText está vacío
                        Toast.makeText(this, "El EditText está vacío", Toast.LENGTH_SHORT).show()
                        editTextVacio = true
                    }

                    // Verificar si algún EditText está vacío
                    if (editTextVacio) {
                        // Mostrar un Toast indicando que al menos un EditText está vacío
                        Toast.makeText(this, "Al menos un EditText está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast indicando que todos los EditText tienen contenido
                        Toast.makeText(this, "Todos los EditText tienen contenido", Toast.LENGTH_SHORT).show()

                        // Aquí puedes continuar con el resto de tu lógica
                    }
                }
                is FrecuenciaFragment -> {
                    val editTextContent = currentFragment.getEditTextContent()
                    var editTextVacio = false
                    if (editTextContent.isNotEmpty()) {
                        // Mostrar un Toast con el contenido del EditText
                        Toast.makeText(this, "Contenido del EditText: $editTextContent", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast si el EditText está vacío
                        Toast.makeText(this, "El EditText está vacío", Toast.LENGTH_SHORT).show()
                        editTextVacio = true
                    }

                    // Verificar si algún EditText está vacío
                    if (editTextVacio) {
                        // Mostrar un Toast indicando que al menos un EditText está vacío
                        Toast.makeText(this, "Al menos un EditText está vacío", Toast.LENGTH_SHORT).show()
                    } else {
                        // Mostrar un Toast indicando que todos los EditText tienen contenido
                        Toast.makeText(this, "Todos los EditText tienen contenido", Toast.LENGTH_SHORT).show()

                        // Aquí puedes continuar con el resto de tu lógica
                    }
                }
            }
        }
    }
}