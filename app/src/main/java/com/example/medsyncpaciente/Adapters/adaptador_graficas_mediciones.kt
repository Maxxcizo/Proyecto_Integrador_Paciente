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
import kotlin.math.roundToInt

class AdaptadorGraficasMediciones(private val context: Context) : RecyclerView.Adapter<AdaptadorGraficasMediciones.ViewHolder>() {

    private var mediciones: MutableList<Medicion> = mutableListOf()

    data class Medicion(val titulo: String, val fechaHora: List<Date>, val valor: List<Float>, val unidadMedida: String, val rangoNormal: Pair<Float, Float>)

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

            println("Medicion $medicion")

            val entries = ArrayList<Entry>()

            for (i in medicion.fechaHora.indices) {
                entries.add(Entry(i.toFloat(), medicion.valor[i]))
            }

            val lineDataSet = LineDataSet(entries, "Medición")
            lineDataSet.color = Color.BLUE
            lineDataSet.setCircleColor(Color.BLUE)
            lineDataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()} ${medicion.unidadMedida}"
                }
            }

            // Calcular el promedio general
            val promedioGeneral = medicion.valor.average().toFloat()
            val entriesPromedioGeneral = ArrayList<Entry>()
            for (i in medicion.fechaHora.indices) {
                entriesPromedioGeneral.add(Entry(i.toFloat(), promedioGeneral))
            }
            val lineDataSetPromedioGeneral = LineDataSet(entriesPromedioGeneral, "Promedio")
            lineDataSetPromedioGeneral.color = Color.MAGENTA
            lineDataSetPromedioGeneral.setDrawCircles(false)
            lineDataSetPromedioGeneral.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()} ${medicion.unidadMedida}"
                }
            }

            // Calcular el promedio semanal
            val entriesPromedioSemanal = ArrayList<Entry>()
            val calendar = Calendar.getInstance()
            var sum = 0f
            var count = 0
            for (i in medicion.fechaHora.indices) {
                val value = medicion.valor[i]
                sum += value
                count++
                if (calendar.get(Calendar.DAY_OF_WEEK) == calendar.firstDayOfWeek || i == medicion.fechaHora.size - 1) {
                    val promedioSemanal = if (count != 0) sum / count else 0f
                    entriesPromedioSemanal.add(Entry(i.toFloat(), promedioSemanal))
                    sum = 0f
                    count = 0
                }
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Rango normal
            val normalLow = medicion.rangoNormal.first
            val normalHigh = medicion.rangoNormal.second
            val entriesNormalLow = medicion.fechaHora.indices.map { Entry(it.toFloat(), normalLow) }
            val entriesNormalHigh = medicion.fechaHora.indices.map { Entry(it.toFloat(), normalHigh) }
            val lineDataSetLow = LineDataSet(entriesNormalLow, "Rango Bajo")
            lineDataSetLow.color = Color.GREEN
            lineDataSetLow.setDrawCircles(false)
            lineDataSetLow.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()} ${medicion.unidadMedida}"
                }
            }
            val lineDataSetHigh = LineDataSet(entriesNormalHigh, "Rango Alto")
            lineDataSetHigh.color = Color.RED
            lineDataSetHigh.setDrawCircles(false)
            lineDataSetHigh.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.roundToInt()} ${medicion.unidadMedida}"
                }
            }

            // Agregar las anomalías
            val anomalías = ArrayList<Entry>()
            for (i in medicion.valor.indices) {
                val valor = medicion.valor[i]
                if (valor < normalLow || valor > normalHigh) {
                    anomalías.add(Entry(i.toFloat(), valor))
                }
            }
            val lineDataSetAnom = LineDataSet(anomalías, "Anomalías")
            lineDataSetAnom.color = Color.YELLOW
            lineDataSetAnom.setDrawCircles(true)
            lineDataSetAnom.circleRadius = 5f
            lineDataSetAnom.setDrawCircleHole(true)

            val combinedData = LineData(lineDataSet, lineDataSetLow, lineDataSetHigh, lineDataSetAnom, lineDataSetPromedioGeneral)

            chart.data = combinedData

            // Configurar marcadores
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
        notifyDataSetChanged()
    }
}
