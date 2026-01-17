package com.ilseon.drift.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import kotlinx.coroutines.delay
import kotlin.dec
import kotlin.math.sqrt

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