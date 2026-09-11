package com.fittrack.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

/**
 * Gráfico de línea simple para mostrar la evolución de un valor (ej: peso máximo) en el tiempo.
 * points: lista de (timestampMillis, valor), ordenada por fecha ascendente.
 */
@Composable
fun ProgressLineChart(
    points: List<Pair<Long, Double>>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    if (points.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (points.isEmpty()) "Todavía no hay datos para graficar"
                else "Registrá al menos 2 entrenamientos para ver la evolución",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val minValue = points.minOf { it.second }
    val maxValue = points.maxOf { it.second }
    val valueRange = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    val fmt = rememberDateFormatter()

    androidx.compose.foundation.layout.Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val leftPad = 8f
            val rightPad = 8f
            val topPad = 16f
            val bottomPad = 16f
            val chartWidth = size.width - leftPad - rightPad
            val chartHeight = size.height - topPad - bottomPad

            val stepX = if (points.size > 1) chartWidth / (points.size - 1) else 0f

            fun xFor(index: Int) = leftPad + stepX * index
            fun yFor(value: Double) = topPad + chartHeight - ((value - minValue) / valueRange * chartHeight).toFloat()

            // Líneas guía horizontales
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(leftPad, topPad),
                end = Offset(leftPad + chartWidth, topPad)
            )
            drawLine(
                color = Color.Gray.copy(alpha = 0.2f),
                start = Offset(leftPad, topPad + chartHeight),
                end = Offset(leftPad + chartWidth, topPad + chartHeight)
            )

            // Línea de progreso
            for (i in 0 until points.size - 1) {
                val start = Offset(xFor(i), yFor(points[i].second))
                val end = Offset(xFor(i + 1), yFor(points[i + 1].second))
                drawLine(color = lineColor, start = start, end = end, strokeWidth = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }

            // Puntos
            points.forEachIndexed { index, (_, value) ->
                drawCircle(color = lineColor, radius = 8f, center = Offset(xFor(index), yFor(value)))
            }
        }

        // Etiquetas de min/max y fechas extremas
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(fmt.format(Date(points.first().first)), style = MaterialTheme.typography.labelSmall)
            Text(fmt.format(Date(points.last().first)), style = MaterialTheme.typography.labelSmall)
        }
        Text(
            "Máx: ${maxValue.toInt()} kg   ·   Mín: ${minValue.toInt()} kg",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun rememberDateFormatter(): SimpleDateFormat =
    androidx.compose.runtime.remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }
