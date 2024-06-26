package com.example.medsyncpaciente

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "REPROGRAM_CITA" -> {
                val documentId = intent.getStringExtra("documentId")
                Toast.makeText(context, "Reprogramar cita para $documentId", Toast.LENGTH_SHORT).show()
                // Aquí puedes implementar la lógica para reprogramar la cita

                // Iniciar la actividad de CitasActivity
                val citasIntent = Intent(context, CitasActivity::class.java).apply {
                    // Puedes pasar cualquier dato adicional si es necesario
                    putExtra("documentId", documentId)
                }
                citasIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context?.startActivity(citasIntent)
            }
            "REJECT_REPROGRAM" -> {
                val documentId = intent.getStringExtra("documentId")
                Toast.makeText(context, "Rechazar reprogramación para $documentId", Toast.LENGTH_SHORT).show()
                // Aquí puedes implementar la lógica para rechazar la reprogramación

                // Iniciar la actividad de AddAppointmentActivity
                val addAppointmentIntent = Intent(context, AddAppointmentActivity::class.java).apply {
                    // Puedes pasar cualquier dato adicional si es necesario
                    putExtra("documentId", documentId)
                }
                addAppointmentIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context?.startActivity(addAppointmentIntent)
            }
        }
    }
}
