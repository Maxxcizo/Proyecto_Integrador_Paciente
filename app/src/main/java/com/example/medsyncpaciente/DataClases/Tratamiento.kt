package com.example.medsyncpaciente.DataClases

import com.google.firebase.firestore.DocumentReference

data class Tratamiento(
    val tratamientoID: String,
    val medico: String,
    val fechaInicio: String,
    val fechaFin: String,
    val diagnostico: String,
    val sintomas: List<String>,
    val recomendaciones: String,
    val tratamientoRef: DocumentReference
)
