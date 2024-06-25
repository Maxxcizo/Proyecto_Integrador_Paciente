package com.example.medsyncpaciente.fragments

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.Adapters.AdaptadorCumplimiento
import com.example.medsyncpaciente.DataClases.Medicamento
import com.example.medsyncpaciente.DataClases.RegistroPorDia
import com.example.medsyncpaciente.DataClases.RegistroToma
import com.example.medsyncpaciente.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class ProgressFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var db: FirebaseFirestore
    private lateinit var adaptadorCumplimiento: AdaptadorCumplimiento
    private lateinit var fabGeneratePdf: FloatingActionButton
    private lateinit var listaMedicamentos: List<Medicamento>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)
        db = FirebaseFirestore.getInstance()

        val sharedPreferences = requireContext().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val pacienteId = sharedPreferences.getString("pacienteId", null)

        listaMedicamentos = mutableListOf()

        val medicamentosRef = db.collection("Paciente").document(pacienteId!!)
            .collection("Medicamentos")

        medicamentosRef.get()
            .addOnSuccessListener { querySnapshot ->
                var documentosProcesados = 0
                val totalDocumentos = querySnapshot.size()

                for (document in querySnapshot) {
                    val nombre = document.id
                    val cantidad = document.getLong("Cantidad")?.toInt() ?: 0
                    val frecuencia = document.getLong("Frecuencia")?.toInt() ?: 1

                    // Inicializar lista de registros por día
                    val registrosPorDia = MutableList(15) { RegistroPorDia("dia${it + 1}", mutableListOf()) }

                    // Obtener la subcolección "Registro de Toma" del documento actual
                    val registrosRef = document.reference.collection("Registro de Toma")

                    registrosRef.get()
                        .addOnSuccessListener { registrosSnapshot ->
                            // Calcular la fecha límite (hace 15 días desde hoy)
                            val hoy = LocalDate.now()
                            val fechaLimite = hoy.minusDays(14)

                            registrosSnapshot.forEach { registro ->
                                val estado = registro.getString("Estado") ?: ""
                                val fechaHora = registro.getString("Fecha_Hora") ?: ""

                                val registroToma = RegistroToma(estado, fechaHora)

                                // Parsear la fecha y hora del registro
                                val fechaRegistro = obtenerFecha(fechaHora)

                                // Filtrar registros dentro de los últimos 15 días
                                if (fechaRegistro.isAfter(fechaLimite) || fechaRegistro.isEqual(fechaLimite)) {
                                    // Calcular el día relativo al día actual
                                    val diasDesdeHoy = hoy.toEpochDay() - fechaRegistro.toEpochDay()
                                    val indiceDia = diasDesdeHoy.toInt()

                                    if (indiceDia in 0..14) {
                                        registrosPorDia[indiceDia].listaRegistros.add(registroToma)
                                    }
                                }
                            }

                            // Ordenar los registros por fecha y hora dentro de cada día
                            registrosPorDia.forEach { dia ->
                                dia.listaRegistros.sortBy { obtenerFechaHora(it.fechaHora) }
                            }

                            // Completar con registros vacíos si es necesario
                            registrosPorDia.forEach { registroPorDia ->
                                while (registroPorDia.listaRegistros.size < frecuencia) {
                                    registroPorDia.listaRegistros.add(RegistroToma("", ""))
                                }
                            }

                            // Crear objeto Medicamento con la lista de registros por día
                            val medicamento = Medicamento(nombre, cantidad, frecuencia, registrosPorDia)
                            (listaMedicamentos as MutableList).add(medicamento)

                            // Incrementar el contador de documentos procesados
                            documentosProcesados++

                            // Verificar si todos los documentos han sido procesados
                            if (documentosProcesados == totalDocumentos) {
                                // Inicializar adaptador
                                adaptadorCumplimiento = AdaptadorCumplimiento(requireActivity(), sharedPreferences)

                                // Pasar la lista de medicamentos al adaptador
                                adaptadorCumplimiento.setMedicamentos(listaMedicamentos)

                                // Configurar RecyclerView
                                recyclerView = view.findViewById(R.id.recyclerView_Cumplimiento)
                                recyclerView.layoutManager = LinearLayoutManager(activity)
                                recyclerView.adapter = adaptadorCumplimiento
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("Error", "Error al obtener registros de toma", exception)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error al obtener medicamentos", exception)
            }

        fabGeneratePdf = view.findViewById(R.id.fab_generate_pdf)
        fabGeneratePdf.setOnClickListener {
            mostrarDialogoConfirmacion()
        }

        return view
    }

    private fun mostrarDialogoConfirmacion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmación")
            .setMessage("¿Desea generar el reporte quincenal?")
            .setPositiveButton("Sí") { dialog, which ->
                generatePDFReport(listaMedicamentos)
            }
            .setNegativeButton("No", null)
            .show()
    }

    // Función para obtener la fecha desde una fecha y hora
    private fun obtenerFecha(fechaHora: String): LocalDate {
        return try {
            // Formato de la fecha y hora
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Parsear la fechaHora a un objeto LocalDateTime
            val fechaHoraLocalDateTime = LocalDateTime.parse(fechaHora, formatter)
            // Devolver solo la parte de la fecha
            fechaHoraLocalDateTime.toLocalDate()
        } catch (e: Exception) {
            Log.e("Error", "Error al parsear fecha: $fechaHora", e)
            LocalDate.MIN // Devolver una fecha inválida para manejo de errores
        }
    }

    // Función para obtener la fecha y hora desde una cadena
    private fun obtenerFechaHora(fechaHora: String): LocalDateTime {
        return try {
            // Formato de la fecha y hora
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            // Parsear la fechaHora a un objeto LocalDateTime
            LocalDateTime.parse(fechaHora, formatter)
        } catch (e: Exception) {
            Log.e("Error", "Error al parsear fecha y hora: $fechaHora", e)
            LocalDateTime.MIN // Devolver una fecha y hora inválida para manejo de errores
        }
    }

    private fun generatePDFReport(listaMedicamentos: List<Medicamento>) {
        println("generatePDFReport llamado")
        val filename = "${System.currentTimeMillis()}_Reporte_Quincenal.pdf"
        val directoryPath = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).toString() + "/MedSyncReports/"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            val directoryCreated = directory.mkdirs()
            if (!directoryCreated) {
                println("Error: No se pudo crear el directorio $directoryPath")
                return
            }
        }

        val filePath = directoryPath + filename
        val file = File(filePath)

        try {
            val writer = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument, PageSize.A4)

            // Agregar contenido al PDF
            document.add(Paragraph("Reporte Quincenal de Toma de Medicamentos"))
            document.add(
                Paragraph("Fecha de creación: ${
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                        Date()
                    )}")
            )
            document.add(Paragraph("\n"))

            // Agregar registros de toma por medicamento
            listaMedicamentos.forEach { medicamento ->
                document.add(Paragraph("Medicamento: ${medicamento.nombre}"))
                medicamento.registrosPorDia.forEach { registroPorDia ->
                    document.add(Paragraph("Día: ${registroPorDia.identificadorDia}"))
                    registroPorDia.listaRegistros.forEachIndexed { index, registro ->
                        val estado = registro.estado.ifEmpty { "No especificado" }
                        document.add(Paragraph("Toma ${index + 1}: $estado"))
                    }
                    document.add(Paragraph("\n"))
                }
            }

            document.close()
            println("Reporte generado en: $filePath")

            // Mostrar un Toast cuando el PDF se haya generado
            Toast.makeText(requireContext(), "Reporte generado en: $filePath", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            println("Error al generar el reporte PDF: ${e.message}")
        }
    }
}
