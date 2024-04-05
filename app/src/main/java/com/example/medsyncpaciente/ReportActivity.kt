package com.example.medsyncpaciente

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorSettings

class ReportActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView
    private lateinit var backIcon: ImageView
    private lateinit var sendButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_report)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById<Toolbar>(R.id.toolbar_report)
        toolbarTitle = findViewById<TextView>(R.id.toolbarsecundario_title)
        backIcon = findViewById(R.id.back_btn)
        sendButton = findViewById(R.id.send_btn)

        toolbar.title = ""
        toolbarTitle.text = "Reporte de Errores"
        setSupportActionBar(toolbar)

        setup()
    }

    private fun setup() {
        backIcon.setOnClickListener {
            onBackPressed()
        }


        sendButton.setOnClickListener{
            // Proceso de enviar el reporte de errores
        }

    }
}