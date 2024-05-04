package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.CambiarCorreoActivity
import com.example.medsyncpaciente.CambiarPasswordActivity
import com.example.medsyncpaciente.ReportActivity
import com.example.medsyncpaciente.PerfilActivity
import com.example.medsyncpaciente.R

class AdaptadorSettings(private val context: Context) : RecyclerView.Adapter<AdaptadorSettings.ViewHolder>() {

    private val settingsTitles = arrayOf("Ver perfil", "Cambiar Contraseña","Cambiar Correo", "Reportar Errores")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_settings, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title = settingsTitles[position]
        holder.settingTitle.text = title

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return settingsTitles.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var settingTitle: TextView = itemView.findViewById(R.id.setting_tv)
    }

    private fun onItemClick(position: Int) {
        val intent = when (position) {
            0 -> Intent(context, PerfilActivity::class.java)
            1 -> Intent(context, CambiarPasswordActivity::class.java)
            2 -> Intent(context, CambiarCorreoActivity::class.java)
            3 -> Intent(context, ReportActivity::class.java)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
        context.startActivity(intent)
    }
}
