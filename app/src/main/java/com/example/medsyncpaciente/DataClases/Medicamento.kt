package com.example.medsyncpaciente.DataClases

data class Medicamento(
    val nombre: String,
    val cantidad: Int,
    val frecuencia: Int,
    val registrosPorDia: List<RegistroPorDia>
)
