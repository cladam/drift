package com.ilseon.drift.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import kotlin.math.sqrt

@Composable
fun ContextualPulseCard(
    hrvValue: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMeasuring: Boolean = false,
    onPulse: (Long) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isMeasuring) {
                Log.d("ContextualPulseCard", "isMeasuring = true, showing CameraPreview")
                CameraPreview(
                    modifier = Modifier.height(200.dp),
                    onPulseDetected = { onPulse(it) },
                    onCameraReady = { android.util.Log.d("ContextualPulseCard", "Camera is ready") }
                )

            } else if (hrvValue != null) {
                Text("HRV", color = CustomTextSecondary)
                Text("${String.format("%.2f", hrvValue)}ms", color = CustomTextPrimary)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Favorite, contentDescription = null, tint = MutedTeal)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "No HRV data found. Tap to measure.",
                        color = CustomTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
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
