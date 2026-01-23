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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.dec

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
    var countdown by remember { mutableIntStateOf(40) }
    val haptic = LocalHapticFeedback.current

    // Track previous state separately
    var previouslyMeasuring by remember { mutableStateOf(false) }

// Haptic on start
    LaunchedEffect(isMeasuring) {
        if (isMeasuring && !previouslyMeasuring) {
            delay(3000) // Wait for camera warmup
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
        previouslyMeasuring = isMeasuring
    }

// Haptic on successful completion
    LaunchedEffect(countdown, isMeasuring) {
        if (countdown == 0 && isMeasuring) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }


    LaunchedEffect(isMeasuring) {
        if (isMeasuring) {
            countdown = 43
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
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(200.dp) // Match CameraPreview height
                ) {
                    CameraPreview(
                        modifier = Modifier.fillMaxSize(), // Fill the box
                        onPulseDetected = { onPulse(it) },
                        onCameraReady = { }
                    )
                    // Show BPM value on top
                    if (bpmValue != null && bpmValue > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "$bpmValue",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
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
                                    Text("%.0f".format(hrvValue), color = CustomTextPrimary)
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
    // Max value based on Kubios scale, providing room at the top.
    val maxStress = 80.0

    val progress = remember(stressIndex) {
        stressIndex?.let { (it / maxStress).toFloat().coerceIn(0f, 1f) } ?: 0f
    }
    Log.d("StressGauge", "stressIndex: $stressIndex, progress: $progress")


    // Marker positions as fractions of the gauge (matching Kubios thresholds)
    val relaxedMarker = 15.0 / maxStress
    val calmMarker = 25.0 / maxStress
    val moderateMarker = 35.0 / maxStress
    val elevatedMarker = 50.0 / maxStress

    val (statusHeader, statusDescription, statusColor) = when {
        stressIndex == null -> Triple("No Data", "Measure to get stress level.", CustomTextSecondary)
        stressIndex < 15 -> Triple("Relaxed", "Excellent recovery, your body is at ease.", StatusLow)
        stressIndex < 25 -> Triple("Calm", "Good state, low physiological stress.", StatusMedium)
        stressIndex < 35 -> Triple("Moderate", "Normal day-to-day stress, body is handling it.", StatusHigh)
        stressIndex < 50 -> Triple("Elevated", "Increased stress, consider taking a break.", StatusUrgent)
        else -> Triple("Exhausted", "High physiological strain, prioritize rest.", StatusUrgent)
    }

    val moodAdvice = when {
        moodScore == null || stressIndex == null -> ""
        moodScore < 0.5f && stressIndex < 25 -> "You might just be tired, not physiologically stressed."
        moodScore < 0.5f && stressIndex > 50 -> "Body is on high alert. Prioritize rest and breathing exercises."
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
                                    relaxedMarker.toFloat() to StatusLow,
                                    calmMarker.toFloat() to StatusMedium,
                                    moderateMarker.toFloat() to StatusHigh,
                                    elevatedMarker.toFloat() to StatusUrgent,
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
