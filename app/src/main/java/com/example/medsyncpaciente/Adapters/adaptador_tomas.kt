
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.DataClases.Medicamento
import com.example.medsyncpaciente.R
import com.google.firebase.firestore.DocumentSnapshot
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdaptadorTomas(
    private val context: Context,
    private val medicamento: Medicamento,
    private val sharedPreferences: SharedPreferences
) : RecyclerView.Adapter<AdaptadorTomas.ViewHolder>() {

    private val frecuencia = medicamento.frecuencia

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diasImageViews: List<ImageView> = listOf(
            itemView.findViewById(R.id.dia7_tv),
            itemView.findViewById(R.id.dia6_tv),
            itemView.findViewById(R.id.dia5_tv),
            itemView.findViewById(R.id.dia4_tv),
            itemView.findViewById(R.id.dia3_tv),
            itemView.findViewById(R.id.dia2_tv),
            itemView.findViewById(R.id.dia1_tv)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        println("AdaptadorTomas onCreateViewHolder llamado")
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_tomas, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("AdaptadorTomas onBindViewHolder llamado para la posición: $position")

        Log.i("Registros por dia", "Registros por día del medicamento: ${medicamento.nombre} son ${medicamento.registrosPorDia.toString()}")

        // Lista para almacenar los estados de las mediciones
        val estadosMediciones = mutableListOf<String>()

        // Iterar sobre los últimos 7 días
        for (i in 1..7) {
            val diaBuscado = "dia$i"

            // Buscar el registro por día correspondiente al día buscado
            val registroPorDia = medicamento.registrosPorDia.find { it.identificadorDia == diaBuscado }

            // Verificar si se encontró el registro por día para el día buscado
            if (registroPorDia != null) {
                // Obtener la lista de registros de toma de ese día
                val tomasDelDia = registroPorDia.listaRegistros

                // Iterar sobre las tomas del día y obtener el estado

                val estado = tomasDelDia[position].estado
                estadosMediciones.add(estado)
                println("Estado de la toma del día $diaBuscado: $estado")
            }
        }

        Log.i("Estados del dia","Position: $position, ${medicamento.nombre} son ${estadosMediciones}")

        updateImageViews(holder, estadosMediciones)

       // generatePDFReport(medicamento)


/*        val pacienteId = sharedPreferences.getString("pacienteId", null)

        if (pacienteId != null) {
            println("Paciente ID encontrado en AdaptadorTomas: $pacienteId")
            CoroutineScope(Dispatchers.Main).launch {
                val registrosPorMedicamento = getRegistrosPorMedicamento(pacienteId)
                updateImageViews(holder, registrosPorMedicamento)
                if (position == frecuencia - 1) {
                    generatePDFReport(registrosPorMedicamento)
                }
            }
        } else {
            println("Paciente ID no encontrado en SharedPreferences en AdaptadorTomas.")
        }*/
    }

    private fun updateImageViews(holder: ViewHolder, estadosMedicamentos: List<String>) {
        println("updateImageViews llamado")
        holder.diasImageViews.forEach { imageView ->
            imageView.setImageResource(R.drawable.cross_icon) // Reemplaza con el ícono correspondiente
        }

        var i = 0

        for (estadosMedicamento in estadosMedicamentos) {
            if (estadosMedicamento.equals("completado")){
                holder.diasImageViews[i].setImageResource(R.drawable.check_icon) // Reemplaza con el ícono correspondiente
                println("El estado: $estadosMedicamento SI es igual a completado")
            }
            else{
                println("El estado: $estadosMedicamento NO es igual a completado")
            }
            i++
        }
    }

    override fun getItemCount(): Int {
        println("AdaptadorTomas getItemCount llamado, frecuencia: $frecuencia")
        return frecuencia
    }
}