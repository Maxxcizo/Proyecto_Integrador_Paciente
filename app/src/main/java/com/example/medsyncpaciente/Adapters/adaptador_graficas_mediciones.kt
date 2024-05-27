package com.example.medsyncpaciente.Adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.medsyncpaciente.CustomMarkerView
import com.example.medsyncpaciente.R
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AdaptadorGraficasMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorGraficasMediciones.ViewHolder>() {

    private var mediciones: MutableList<Medicion> = mutableListOf()

    data class Medicion(
        val titulo: String,
        val fechaHora: List<Date>,
        val valorS: List<Float> = listOf(),
        val valorD: List<Float> = listOf(),
        val valorF: List<Float> = listOf(),
        val eventosAnomalosS: List<Entry>,
        val eventosAnomalosD: List<Entry>,
        val eventosAnomalosF: List<Entry>,
        val unidadMedida: String,
        val rangoNormal: Pair<Float, Float>
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.rv_graficas_mediciones, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicion = mediciones[position]
        holder.tituloGraficaMedicion.text = medicion.titulo
        holder.setupLineChart(medicion)
    }

    override fun getItemCount(): Int {
        return mediciones.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tituloGraficaMedicion: TextView = itemView.findViewById(R.id.tituloGraficaMedicion)
        var chart: LineChart = itemView.findViewById(R.id.chart)

        fun setupLineChart(medicion: Medicion) {
            val entriesS = ArrayList<Entry>()
            val entriesD = ArrayList<Entry>()
            val entriesF = ArrayList<Entry>()
            val entriesAnomalosS = ArrayList<Entry>()
            val entriesAnomalosD = ArrayList<Entry>()
            val entriesAnomalosF = ArrayList<Entry>()

            for (i in medicion.fechaHora.indices) {
                entriesS.add(Entry(i.toFloat(), medicion.valorS[i]))
                println("Medición normal sistólica ${medicion.valorS[i]}")
                if (medicion.valorD.isNotEmpty()) {
                    entriesD.add(Entry(i.toFloat(), medicion.valorD[i]))
                    println("Medición normal diastólica ${medicion.valorD[i]}")
                }
                if (medicion.valorF.isNotEmpty()) {
                    entriesF.add(Entry(i.toFloat(), medicion.valorF[i]))
                    println("Medición normal frecuencia ${medicion.valorF[i]}")
                }
            }

            var lineDataSetS = LineDataSet(entriesS, "")
            when(medicion.titulo) {
                "Presion Arterial" -> {
                    lineDataSetS = LineDataSet(entriesS, "Sistólica")
                }
                else -> {
                    lineDataSetS = LineDataSet(entriesS, "Medición")
                }
            }

            val lineDataSetD = LineDataSet(entriesD, "Diastólica")
            val lineDataSetF = LineDataSet(entriesF, "Frecuencia Cardíaca")
            val lineDataSetAnomS = LineDataSet(medicion.eventosAnomalosS, "Anomalías")
            val lineDataSetAnomD = LineDataSet(medicion.eventosAnomalosD, "Anomalías")
            val lineDataSetAnomF = LineDataSet(medicion.eventosAnomalosF, "Anomalías")

            lineDataSetS.color = Color.BLUE
            lineDataSetS.setCircleColor(Color.BLUE)
            lineDataSetD.color = Color.BLUE
            lineDataSetD.setCircleColor(Color.BLUE)
            lineDataSetF.color = Color.BLACK
            lineDataSetF.setCircleColor(Color.BLACK)

            lineDataSetAnomS.color = Color.CYAN
            lineDataSetAnomS.setCircleColor(Color.CYAN)

            lineDataSetAnomD.color = Color.CYAN
            lineDataSetAnomD.setCircleColor(Color.CYAN)

            lineDataSetAnomF.color = Color.CYAN
            lineDataSetAnomF.setCircleColor(Color.CYAN)

            val lineDataSetPromedioGeneral: LineDataSet? = if (medicion.titulo != "Presion Arterial") {
                val promedioGeneral = medicion.valorS.average().toFloat()
                val entriesPromedioGeneral = ArrayList<Entry>()
                for (i in medicion.fechaHora.indices) {
                    entriesPromedioGeneral.add(Entry(i.toFloat(), promedioGeneral))
                }
                val dataSet = LineDataSet(entriesPromedioGeneral, "Promedio")
                dataSet.color = Color.MAGENTA
                dataSet.setDrawCircles(false)
                dataSet.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "${value.toInt()} ${medicion.unidadMedida}"
                    }
                }
                dataSet
            } else {
                null
            }

            val entriesPromedioSemanal = ArrayList<Entry>()
            val calendar = Calendar.getInstance()
            var sum = 0f
            var count = 0
            for (i in medicion.fechaHora.indices) {
                val value = medicion.valorS[i]
                sum += value
                count++
                if (calendar.get(Calendar.DAY_OF_WEEK) == calendar.firstDayOfWeek || i == medicion.fechaHora.size - 1) {
                    val promedioSemanal = if (count != 0) sum / count else 0f
                    entriesPromedioSemanal.add(Entry(i.toFloat(), promedioSemanal
                    ))
                    sum = 0f
                    count = 0
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val normalLow = medicion.rangoNormal.first
            val normalHigh = medicion.rangoNormal.second
            val entriesNormalLow = medicion.fechaHora.indices.map { Entry(it.toFloat(), normalLow) }
            val entriesNormalHigh = medicion.fechaHora.indices.map { Entry(it.toFloat(), normalHigh) }
            val lineDataSetLow = LineDataSet(entriesNormalLow, "Rango Bajo")
            lineDataSetLow.color = Color.GREEN
            lineDataSetLow.setDrawCircles(false)
            lineDataSetLow.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} ${medicion.unidadMedida}"
                }
            }
            val lineDataSetHigh = LineDataSet(entriesNormalHigh, "Rango Alto")
            lineDataSetHigh.color = Color.RED
            lineDataSetHigh.setDrawCircles(false)
            lineDataSetHigh.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()} ${medicion.unidadMedida}"
                }
            }

            val combinedData = if (medicion.valorD.isNotEmpty() && medicion.valorF.isNotEmpty()) {
                if (lineDataSetPromedioGeneral != null) {
                    LineData(
                        lineDataSetS, lineDataSetD, lineDataSetF, lineDataSetLow,
                        lineDataSetHigh, lineDataSetAnomS, lineDataSetAnomD, lineDataSetAnomF, lineDataSetPromedioGeneral
                    )
                } else {
                    LineData(
                        lineDataSetS, lineDataSetD, lineDataSetF, lineDataSetLow,
                        lineDataSetHigh, lineDataSetAnomS, lineDataSetAnomD, lineDataSetAnomF
                    )
                }
            } else {
                if (lineDataSetPromedioGeneral != null) {
                    LineData(
                        lineDataSetS, lineDataSetLow, lineDataSetHigh,
                        lineDataSetAnomS, lineDataSetAnomD, lineDataSetAnomF, lineDataSetPromedioGeneral
                    )
                } else {
                    LineData(
                        lineDataSetS, lineDataSetLow, lineDataSetHigh,
                        lineDataSetAnomS, lineDataSetAnomD, lineDataSetAnomF
                    )
                }
            }

            println("Combined data: $combinedData")
            println("Anomalias data: $lineDataSetAnomS")

            chart.data = combinedData

            val dateFormatter = SimpleDateFormat("MM/dd", Locale.getDefault())
            val xAxisLabels = medicion.fechaHora.map { dateFormatter.format(it) }
            val customMarkerView = CustomMarkerView(context, R.layout.marker_view, xAxisLabels)
            chart.marker = customMarkerView
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = (medicion.fechaHora.size - 1).toFloat()

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < medicion.fechaHora.size) {
                        dateFormatter.format(medicion.fechaHora[index])
                    } else {
                        ""
                    }
                }
            }

            chart.setTouchEnabled(true)
            chart.description.isEnabled = false
            chart.invalidate()
        }
    }

    fun addMedicion(medicion: Medicion) {
        mediciones.add(medicion)
        mediciones.sortBy { it.titulo }
        notifyDataSetChanged()
    }
}
