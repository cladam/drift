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
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.StatusHigh

@Composable
fun TrendSparklineCard(
    modifier: Modifier = Modifier,
    title: String,
    data: List<Double>,
) {
    if (data.size < 2) return // Don't show the card if there is not enough data

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = CustomTextSecondary)
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
    val color = StatusHigh
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
            // Add vertical padding to the line for better visual breathing room.
            val range = maxVal - minVal
            val verticalPadding = if (range > 0) range * 0.1 else 1.0 // 10% padding

            val paddedMin = (minVal - verticalPadding).coerceAtLeast(0.0)
            val paddedMax = maxVal + verticalPadding
            val displayRange = if (paddedMax - paddedMin > 0) paddedMax - paddedMin else 1.0

            val path = Path()

            data.forEachIndexed { index, value ->
                val x = size.width * (index.toFloat() / (data.size - 1))
                val y = size.height * (1 - ((value - paddedMin) / displayRange).toFloat())

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 5f, cap = StrokeCap.Round)
            )
        }
    }
}
