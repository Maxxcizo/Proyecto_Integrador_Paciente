import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AdaptadorTomas(
    private val context: Context,
    private val tomas: List<Int>,
    private val medicamento: String,
    private val sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<AdaptadorTomas.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diasImageViews: List<ImageView> = listOf(
            itemView.findViewById(R.id.dia1_tv),
            itemView.findViewById(R.id.dia2_tv),
            itemView.findViewById(R.id.dia3_tv),
            itemView.findViewById(R.id.dia4_tv),
            itemView.findViewById(R.id.dia5_tv),
            itemView.findViewById(R.id.dia6_tv),
            itemView.findViewById(R.id.dia7_tv)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_tomas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val toma = tomas[position]

        val pacienteId = sharedPreferences.getString("pacienteId", null)
        println("Paciente ID: $pacienteId")
        if (pacienteId != null) {
            val registroTomaRef = db.collection("Paciente").document(pacienteId)
                .collection("Medicamentos").document(medicamento)
                .collection("Registro de Toma")

            val oneWeekAgo = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -6)
            }.time

            println("Consultando registros desde: ${dateTimeFormat.format(oneWeekAgo)}")

            registroTomaRef.whereGreaterThanOrEqualTo("Fecha_Hora", dateTimeFormat.format(oneWeekAgo))
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result
                        if (documents != null && !documents.isEmpty) {
                            println("Documentos obtenidos: ${documents.size()}")
                            val registrosPorFecha = documents.groupBy {
                                dateFormat.format(dateTimeFormat.parse(it.getString("Fecha_Hora")!!))
                            }

                            registrosPorFecha.forEach { (fecha, registros) ->
                                println("Fecha: $fecha, Registros: ${registros.size}")
                                registros.forEach { registro ->
                                    println("    Registro: ${registro.data}")
                                }
                            }

                            // Generar el reporte en PDF
                            generatePDFReport(registrosPorFecha)
                        } else {
                            println("No se encontraron documentos.")
                        }
                    } else {
                        println("Error al obtener documentos: ${task.exception}")
                    }
                }
        } else {
            println("Paciente ID no encontrado en SharedPreferences.")
        }
    }

    private fun generatePDFReport(registrosPorFecha: Map<String, List<DocumentSnapshot>>) {
        val filename = "${System.currentTimeMillis()}_Reporte_Quincenal.pdf"
        val directoryPath = Environment.getExternalStorageDirectory().toString() + "/MedSyncReports/"
        val directory = File(directoryPath)
        if (!directory.exists()) {
            if (checkManageExternalStoragePermission()) {
                val directoryCreated = directory.mkdirs()
                if (!directoryCreated) {
                    println("Error: No se pudo crear el directorio $directoryPath")
                    return
                }
            } else {
                println("Error: Permiso MANAGE_EXTERNAL_STORAGE no concedido")
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
            document.add(Paragraph("Fecha de creación: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}"))
            document.add(Paragraph("\n"))

            // Agregar registros de toma
            registrosPorFecha.forEach { (fecha, registros) ->
                document.add(Paragraph("Fecha: $fecha"))
                registros.forEachIndexed { index, registro ->
                    val estado = registro.getString("Estado") ?: "No especificado"
                    document.add(Paragraph("Toma ${index + 1}: $estado"))
                }
                document.add(Paragraph("\n"))
            }

            document.close()
            println("Reporte generado en: $filePath")
        } catch (e: Exception) {
            println("Error al generar el reporte PDF: ${e.message}")
        }
    }

    private fun checkManageExternalStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return true // El permiso no es necesario en versiones anteriores a Android 11
        }
        return if (ContextCompat.checkSelfPermission(context, Manifest.permission.MANAGE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            true
        } else {
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE), MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
            false
        }
    }

    companion object {
        private const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 100
    }

    override fun getItemCount(): Int {
        return tomas.size
    }
}
