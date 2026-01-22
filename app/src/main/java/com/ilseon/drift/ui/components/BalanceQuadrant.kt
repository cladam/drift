package com.ilseon.drift.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.StatusHigh

@Composable
fun BalanceQuadrantCard(
    hrv: Double?,
    stressIndex: Double?,
    previousHrv: Double?,
    previousStressIndex: Double?,
    allLogs: List<DriftLog>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Balance Quadrant",
                style = MaterialTheme.typography.titleMedium,
                color = CustomTextSecondary
            )
            Spacer(Modifier.height(8.dp))
            if (hrv != null && stressIndex != null) {
                BalanceQuadrant(
                    hrv = hrv,
                    stressIndex = stressIndex,
                    previousHrv = previousHrv,
                    previousStressIndex = previousStressIndex,
                    allLogs = allLogs
                )
            } else {
                Text(
                    "Measure your pulse to see your position.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CustomTextSecondary,
                    modifier = Modifier.padding(8.dp).fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BalanceQuadrant(
    hrv: Double,
    stressIndex: Double,
    previousHrv: Double?,
    previousStressIndex: Double?,
    allLogs: List<DriftLog>,
    modifier: Modifier = Modifier,
) {
    // Dynamic axis calculation
    val hrvAxisMax = (allLogs.mapNotNull { it.hrvValue }.maxOrNull() ?: hrv)
        .let { (it * 1.2).coerceAtLeast(100.0) }
    val siAxisMax = (allLogs.mapNotNull { it.stressIndex }.maxOrNull() ?: stressIndex)
        .let { (it * 1.2).coerceAtLeast(30.0) }

    val density = LocalDensity.current
    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = with(density) { 10.sp.toPx() }
        color = CustomTextSecondary.copy(alpha = 0.5f).toArgb()
    }

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(200.dp)
    ) {
        val midX = size.width / 2
        val midY = size.height / 2

        // Draw quadrant lines (dashed)
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = CustomTextSecondary.copy(alpha = 0.3f),
            start = Offset(midX, 0f),
            end = Offset(midX, size.height),
            pathEffect = pathEffect
        )
        drawLine(
            color = CustomTextSecondary.copy(alpha = 0.3f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            pathEffect = pathEffect
        )

        // Quadrant Labels
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            nativeCanvas.drawText("High Stress, Low Resilience", midX + 10, midY + 20, textPaint)
            nativeCanvas.drawText("Low Stress, Low Resilience", 10f, midY + 20, textPaint)
            nativeCanvas.drawText("Low Stress, High Resilience", 10f, 20f, textPaint)
            nativeCanvas.drawText("High Stress, High Resilience", midX + 10, 20f, textPaint)
        }

        // Calculate current point coordinates
        val currentX = (stressIndex / siAxisMax * size.width).toFloat().coerceIn(0f, size.width)
        val currentY = (size.height - (hrv / hrvAxisMax * size.height)).toFloat().coerceIn(0f, size.height)

        // Draw trace line and previous dot if data is available
        if (previousHrv != null && previousStressIndex != null) {
            val prevX = (previousStressIndex / siAxisMax * size.width).toFloat().coerceIn(0f, size.width)
            val prevY = (size.height - (previousHrv / hrvAxisMax * size.height)).toFloat().coerceIn(0f, size.height)

            // Draw trace line
            drawLine(
                color = StatusHigh.copy(alpha = 0.5f),
                start = Offset(prevX, prevY),
                end = Offset(currentX, currentY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Draw previous dot
            drawPoints(
                points = listOf(Offset(prevX, prevY)),
                pointMode = PointMode.Points,
                color = StatusHigh.copy(alpha = 0.5f),
                strokeWidth = 16f,
                cap = StrokeCap.Round
            )
        }

        // Draw the current dot
        drawPoints(
            points = listOf(Offset(currentX, currentY)),
            pointMode = PointMode.Points,
            color = StatusHigh,
            strokeWidth = 24f,
            cap = StrokeCap.Round
        )
    }
}
