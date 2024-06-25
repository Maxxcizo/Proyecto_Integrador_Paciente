package com.example.medsyncpaciente.DataClases

data class RegistroPorDia(
    var identificadorDia: String, // Identificador del día (por ejemplo, "dia1", "dia2", ...)
    val listaRegistros: MutableList<RegistroToma>
)
