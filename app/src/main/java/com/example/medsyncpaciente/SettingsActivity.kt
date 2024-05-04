package com.example.medsyncpaciente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var logoutButton: Button
    private lateinit var nombreTV: TextView
    private lateinit var bd: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        bd = FirebaseFirestore.getInstance()
        toolbar = findViewById(R.id.toolbar_settings)
        toolbarTitle = findViewById(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        logoutButton = findViewById(R.id.logout_btn)
        nombreTV = findViewById(R.id.name_tv)

        // Configuracion del Recycler View y su adaptador
        recyclerView = findViewById(R.id.recyclerView_Settings)
        val adapter = AdaptadorSettings(this)

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        toolbar.title = ""
        toolbarTitle.text = "Settings"
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
                    val nombre = documentSnapshot.getString("Nombre(s)")
                    if (nombre != null) {
                        nombreTV.text = "Hola, $nombre!"
                    } else {
                        nombreTV.text = "Hola!"
                    }
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

        logoutButton.setOnClickListener{
            val dialogo1 = AlertDialog.Builder(this)
            dialogo1.setTitle("Aviso")
            dialogo1.setMessage("¿Quieres cerrar sesión?")
            dialogo1.setCancelable(false)
            dialogo1.setPositiveButton("Confirmar") { dialog, _ ->
                aceptar()
                dialog.dismiss()
            }
            dialogo1.setNegativeButton("Cancelar") { dialog, _ ->
                cancelar()
                dialog.dismiss()
            }
            dialogo1.show()
        }
    }

    private fun aceptar() {
        val t = Toast.makeText(this, "Aceptaste.", Toast.LENGTH_SHORT)
        t.show()

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.clear()
        prefs.apply()

        FirebaseAuth.getInstance().signOut()

        val loginIntent = Intent(this, InicioDeSesion::class.java)
        // Agregar flags para limpiar el stack de actividades
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
        finish()
    }

    private fun cancelar() {
        val t = Toast.makeText(this, "Rechazaste.", Toast.LENGTH_SHORT)
        t.show()
    }
}
