package com.ilseon.drift.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.theme.BorderQuiet
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.EnergyHigh
import com.ilseon.drift.ui.theme.EnergyLow
import com.ilseon.drift.ui.theme.EnergyMedium
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedDetail
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInModal(
    onDismissRequest: () -> Unit,
    onLog: (Float, String, Double?, Int?, Double?) -> Unit,
    latestCheckIn: DriftLog?,
    hrv: Double? = null,
    bpm: Int? = null,
    stressIndex: Double? = null
) {
    var moodScore by remember { mutableFloatStateOf(latestCheckIn?.moodScore ?: 0.5f) }
    var energyLevel by remember { mutableStateOf(latestCheckIn?.energyLevel ?: "MEDIUM") }
    val sliderTrackGradient = Brush.horizontalGradient(colors = listOf(StatusUrgent, StatusMedium, StatusHigh))

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LightGrey
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hrv != null || bpm != null || stressIndex != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (bpm != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$bpm", style = MaterialTheme.typography.headlineMedium, color = CustomTextPrimary)
                                Text("BPM", color = CustomTextSecondary)
                            }
                        }
                        if (hrv != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${String.format("%.1f", hrv)}", style = MaterialTheme.typography.headlineMedium, color = CustomTextPrimary)
                                Text("HRV (ms)", color = CustomTextSecondary)
                            }
                        }
                        if (stressIndex != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${"%.1f".format(stressIndex)}", style = MaterialTheme.typography.headlineMedium, color = CustomTextPrimary)
                                Text("Stress Index", color = CustomTextSecondary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderQuiet, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(text = "How's your headspace?", color = CustomTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = moodScore,
                    onValueChange = { moodScore = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MutedTeal, // This will be overridden by the custom track
                        inactiveTrackColor = MutedTeal.copy(alpha = 0.5f)
                    ),
                    track = {
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .fillMaxWidth()
                                .background(sliderTrackGradient, shape = RoundedCornerShape(2.dp))
                        )
                    },
                    thumb = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .border(2.dp, BorderQuiet, CircleShape)
                                .background(MutedDetail, CircleShape)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Energy Level", color = CustomTextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val energyLevels = listOf("LOW", "MEDIUM", "HIGH")
                    val energyLevelColors = mapOf(
                        "LOW" to EnergyLow,
                        "MEDIUM" to EnergyMedium,
                        "HIGH" to EnergyHigh
                    )

                    energyLevels.forEach { level ->
                        val isSelected = energyLevel == level
                        val color = energyLevelColors[level] ?: MutedTeal
                        OutlinedButton(
                            onClick = { energyLevel = level },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
                            ),
                            border = BorderStroke(1.dp, if (isSelected) color else BorderQuiet)
                        ) {
                            Text(
                                text = level.lowercase().replaceFirstChar { it.uppercase() },
                                color = if (isSelected) color else CustomTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onLog(moodScore, energyLevel, hrv, bpm, stressIndex) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedTeal)
                ) {
                    Text(text = "Log", color = Color.White)
                }
            }
        }
    }
}
