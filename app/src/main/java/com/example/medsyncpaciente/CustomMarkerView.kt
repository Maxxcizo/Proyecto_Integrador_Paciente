package com.example.medsyncpaciente

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.example.medsyncpaciente.R

class CustomMarkerView(context: Context, layoutResource: Int, private val xAxisLabels: List<String>) :
    MarkerView(context, layoutResource) {

    private val tvDate: TextView = findViewById(R.id.tvDate)
    private val tvValue: TextView = findViewById(R.id.tvValue)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let {
            val index = e.x.toInt()
            val date = if (index >= 0 && index < xAxisLabels.size) xAxisLabels[index] else ""
            val value = e.y.toString()
            tvDate.text = "Fecha: $date"
            tvValue.text = "Valor: $value"
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}
