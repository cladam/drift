package com.ilseon.drift.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusLow
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent
import kotlinx.coroutines.delay
import kotlin.text.toFloat

@Composable
fun ContextualPulseCard(
    hrvValue: Double?,
    bpmValue: Int?,
    onClick: () -> Unit,
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier,
    isMeasuring: Boolean = false,
    onPulse: (Long) -> Unit = {}
) {
    var countdown by remember { mutableIntStateOf(25) }

    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            countdown = 25
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isMeasuring) { onClick() },
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        if (isMeasuring) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Pulse & HRV", color = CustomTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$countdown s", color = CustomTextSecondary)
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = CustomTextSecondary,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { onCancel() }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                CameraPreview(
                    modifier = Modifier.height(200.dp),
                    onPulseDetected = { onPulse(it) },
                    onCameraReady = { }
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = MutedTeal)
                Spacer(Modifier.width(8.dp))
                Column(verticalArrangement = Arrangement.Center) {
                    Text("Pulse & HRV", color = CustomTextSecondary)
                    when {
                        bpmValue != null || hrvValue != null -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (bpmValue != null) {
                                    Text("$bpmValue", color = CustomTextPrimary)
                                    Text(" bpm", color = CustomTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                                if (bpmValue != null && hrvValue != null) {
                                    Text(" · ", color = CustomTextSecondary)
                                }
                                if (hrvValue != null) {
                                    Text("${"%.0f".format(hrvValue)}", color = CustomTextPrimary)
                                    Text(" ms", color = CustomTextSecondary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        else -> {
                            Text("Tap to measure", color = CustomTextSecondary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StressGauge(
    stressIndex: Double?,
    moodScore: Float?,
    modifier: Modifier = Modifier
) {
    // Max value should match your highest meaningful threshold
    val maxStress = 40.0 // Slightly above "Exhaustion" threshold (30)

    val progress = remember(stressIndex) {
        stressIndex?.let { (it / maxStress).toFloat().coerceIn(0f, 1f) } ?: 0f
    }
    Log.d("StressGauge", "stressIndex: $stressIndex, progress: $progress")


    // Marker positions as fractions of the gauge (matching thresholds)
    val restMarker = 12.0 / maxStress      // 0.3
    val loadMarker = 22.0 / maxStress      // 0.55
    val highStressMarker = 30.0 / maxStress // 0.75

    val (statusHeader, statusDescription, statusColor) = when {
        stressIndex == null -> Triple("No Data", "Measure to get stress level.", CustomTextSecondary)
        stressIndex < 12 -> Triple("Rest", "Relaxed state, good recovery.", StatusLow)
        stressIndex <= 22 -> Triple("Load", "Normal daily activity level.", StatusMedium)
        stressIndex <= 30 -> Triple("High Stress", "The body is in a defensive/alarm state.", StatusHigh)
        else -> Triple("Exhaustion", "Extreme load, risk of overtraining.", StatusUrgent)
    }

    val moodAdvice = when {
        moodScore == null || stressIndex == null -> ""
        moodScore < 0.5f && stressIndex < 12 -> "You might just be tired, not stressed."
        moodScore < 0.5f && stressIndex > 30 -> "Body is on high alert. Try breathing exercises."
        else -> ""
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(statusHeader, color = statusColor, style = MaterialTheme.typography.titleMedium)
                if (stressIndex != null) {
                    Text("${"%.0f".format(stressIndex)} SI", color = CustomTextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = statusDescription,
                color = CustomTextSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
// Gauge bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                // Full gradient background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                colorStops = arrayOf(
                                    0.0f to StatusLow,
                                    restMarker.toFloat() to StatusLow,
                                    loadMarker.toFloat() to StatusMedium,
                                    highStressMarker.toFloat() to StatusHigh,
                                    1.0f to StatusUrgent
                                )
                            )
                        ).clip(
                            // Clip to only show the progress portion
                            androidx.compose.ui.graphics.RectangleShape
                        )
                )
                // Overlay to hide the unfilled portion
                Box(
                    modifier = Modifier
                        .fillMaxWidth(1f - progress)
                        .fillMaxHeight()
                        .align(Alignment.CenterEnd)
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }

            if (moodAdvice.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = moodAdvice,
                    color = CustomTextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

fun calculateRmssdFromIntervals(intervals: List<Long>): Double {
    if (intervals.size < 2) return 0.0

    // Remove outliers using IQR method
    val sorted = intervals.sorted()
    val q1 = sorted[sorted.size / 4]
    val q3 = sorted[3 * sorted.size / 4]
    val iqr = q3 - q1
    val lowerBound = q1 - 1.5 * iqr
    val upperBound = q3 + 1.5 * iqr

    val filtered = intervals.filter { it >= lowerBound && it <= upperBound }

    if (filtered.size < 2) return 0.0

    val squaredDiffs = filtered.zipWithNext { a, b -> ((b - a) * (b - a)).toDouble() }
    return kotlin.math.sqrt(squaredDiffs.average())
}

fun calculateBpmFromIntervals(intervals: List<Long>): Int {
    if (intervals.isEmpty()) return 0
    val avgInterval = intervals.average()
    return (60000.0 / avgInterval).toInt()
}

fun calculateStressIndex(rrIntervals: List<Long>): Double {
    if (rrIntervals.size < 2) return 0.0

    // 1. Calculate original Baevsky Stress Index
    val maxRR = rrIntervals.maxOrNull()?.toDouble() ?: 0.0
    val minRR = rrIntervals.minOrNull()?.toDouble() ?: 0.0
    val mxDMn = (maxRR - minRR) / 1000.0 // seconds
    if (mxDMn == 0.0) return 0.0

    val bins = rrIntervals.map { (it / 50) * 50 }
    val modeBin = bins.groupBy { it }.maxByOrNull { it.value.size }?.key ?: 0L
    val mo = modeBin / 1000.0 // seconds
    if (mo == 0.0) return 0.0

    val modeCount = bins.count { it == modeBin }
    val aMo = (modeCount.toDouble() / rrIntervals.size) * 100.0

    val baevskySI = aMo / (2.0 * mo * mxDMn)

    // Kubios Stress Index, take square root of Baevsky Stress Index
    val kubiosSI = kotlin.math.sqrt(baevskySI)
    
    // Return 0 if the result is not a finite number
    return if(kubiosSI.isFinite()) kubiosSI else 0.0
}
