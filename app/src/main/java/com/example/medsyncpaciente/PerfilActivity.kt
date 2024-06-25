package com.example.medsyncpaciente

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore

class PerfilActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var saveButton: Button
    private lateinit var nombre_tv: TextView
    private lateinit var ap_tv: TextView
    private lateinit var am_tv: TextView
    private lateinit var correo_tv: TextView
    private lateinit var tel_et: EditText
    private lateinit var bd: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_perfil)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_profile)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        saveButton = findViewById(R.id.save_btn)
        nombre_tv = findViewById(R.id.nombre_tv)
        ap_tv = findViewById(R.id.ap_tv)
        am_tv = findViewById(R.id.am_tv)
        correo_tv = findViewById(R.id.correo_tv)
        tel_et = findViewById(R.id.tel_et)
        bd = FirebaseFirestore.getInstance()

        toolbar.title = ""
        toolbarTitle.text = "Perfil"
        setSupportActionBar(toolbar)

        setup()

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)

        // Verificar si el correo electrónico ya está registrado
        bd.collection("Paciente").whereEqualTo("Correo", email).get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // El correo electrónico no está registrado, continuar con el registro
                } else {
                    // El correo electrónico ya está registrado, mostrar mensaje de alerta
                    val documentSnapshot = documents.documents.first() // Obtener el primer documento
                    nombre_tv.setText("Nombre: "+documentSnapshot.getString("Nombre(s)"))
                    ap_tv.setText("Apellido Paterno: "+documentSnapshot.getString("Apellido Paterno"))
                    am_tv.setText("Apellido Materno: "+documentSnapshot.getString("Apellido Materno"))
                    correo_tv.setText("Correo: "+documentSnapshot.getString("Correo"))
                    tel_et.setText(documentSnapshot.getString("Telefono"))
                }
            }
            .addOnFailureListener { exception ->
                // Manejar el error
                Toast.makeText(this, "Error al verificar el correo electrónico: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }


        saveButton.setOnClickListener{
            val tel = tel_et.text.toString()

            if (tel.length != 10) {
                tel_et.error = "El teléfono debe tener 10 dígitos."
                return@setOnClickListener
            }

            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
            val email = prefs.getString("email", null)

            // Obtener la referencia al documento del usuario
            bd.collection("Paciente").whereEqualTo("Correo", email).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documentSnapshot = documents.documents.first() // Obtener el primer documento
                        val userId = documentSnapshot.id
                        val userData = mapOf(
                            "Telefono" to tel
                        )

                        // Actualizar el numero de telefono en la base de datos
                        bd.collection("Paciente").document(userId).update(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Número de teléfono actualizado correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this, "Error al actualizar el número de teléfono: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }

    }
}