package com.ilseon.drift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedGreen
import com.ilseon.drift.ui.theme.MutedRed
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import kotlin.compareTo
import kotlin.math.abs
import kotlin.times

@Composable
fun TrendSparklineCard(
    modifier: Modifier = Modifier,
    title: String,
    data: List<Double>,
    unit: String = "",
    higherIsBetter: Boolean,
    formatValue: (Double) -> String = { "%.1f".format(it) }
) {
    if (data.size < 2) return

    val average = data.average()
    val currentValue = data.last()
    val deviation = currentValue - average
    val deviationString = if (deviation >= 0) "+${formatValue(deviation)}" else "-${formatValue(abs(deviation))}"

    val deviationColor = when {
        deviation > 0 && higherIsBetter -> MutedGreen
        deviation > 0 && !higherIsBetter -> MutedRed
        deviation < 0 && higherIsBetter -> MutedRed
        deviation < 0 && !higherIsBetter -> MutedGreen
        else -> CustomTextSecondary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = CustomTextSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (unit.isEmpty()) formatValue(currentValue) else "${formatValue(currentValue)} $unit",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$deviationString vs avg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = deviationColor
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TrendSparkline(data = data)
        }
    }
}

@Composable
private fun TrendSparkline(
    modifier: Modifier = Modifier,
    data: List<Double>,
) {
    val color = MutedTeal//StatusHigh
    val maxVal = data.maxOrNull() ?: 0.0
    val minVal = data.minOrNull() ?: 0.0

    Row(
        modifier = modifier.height(60.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.height(60.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            Text(text = "%.0f".format(maxVal), style = MaterialTheme.typography.labelSmall, color = CustomTextSecondary)
            Text(text = "%.0f".format(minVal), style = MaterialTheme.typography.labelSmall, color = CustomTextSecondary)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Canvas(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
        ) {
            val range = maxVal - minVal
            val verticalPadding = if (range > 0) range * 0.1 else 1.0

            val paddedMin = (minVal - verticalPadding).coerceAtLeast(0.0)
            val paddedMax = maxVal + verticalPadding
            val displayRange = if (paddedMax - paddedMin > 0) paddedMax - paddedMin else 1.0

            val linePath = Path()
            val fillPath = Path()

            data.forEachIndexed { index, value ->
                val x = size.width * (index.toFloat() / (data.size - 1))
                val y = size.height * (1 - ((value - paddedMin) / displayRange).toFloat())

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, size.height) // Start at bottom
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Close the fill path to the bottom
            fillPath.lineTo(size.width, size.height)
            fillPath.lineTo(0f, size.height)
            fillPath.close()

            // Draw fill first (behind the line)
            drawPath(
                path = fillPath,
                color = color.copy(alpha = 0.8f)
            )

            // Draw line on top
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }
    }
}