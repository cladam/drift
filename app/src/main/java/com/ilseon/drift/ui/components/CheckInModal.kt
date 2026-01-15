package com.ilseon.drift.ui.components

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedDetail
import com.ilseon.drift.ui.theme.MutedRed
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInModal(
    onDismissRequest: () -> Unit,
    onLog: (Float, String) -> Unit,
    latestCheckIn: DriftLog?,
) {
    var sliderPosition by remember { mutableFloatStateOf(latestCheckIn?.moodScore ?: 0.5f) }
    var energyLevel by remember { mutableStateOf(latestCheckIn?.energyLevel ?: "MEDIUM") }
    val sliderTrackGradient = Brush.horizontalGradient(colors = listOf(StatusUrgent, StatusMedium))

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = LightGrey
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "How's your headspace?", color = CustomTextPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        activeTrackColor = MutedTeal, // This will be overridden by the custom track
                        inactiveTrackColor = MutedTeal.copy(alpha = 0.5f)
                    ),
                    track = { sliderPositions ->
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
                    Button(onClick = { energyLevel = "LOW" }, colors = ButtonDefaults.buttonColors(containerColor = if (energyLevel == "LOW") MutedTeal else MutedRed)) { Text("Low", color = Color.White) }
                    Button(onClick = { energyLevel = "MEDIUM" }, colors = ButtonDefaults.buttonColors(containerColor = if (energyLevel == "MEDIUM") MutedTeal else MutedRed)) { Text("Medium", color = Color.White) }
                    Button(onClick = { energyLevel = "HIGH" }, colors = ButtonDefaults.buttonColors(containerColor = if (energyLevel == "HIGH") MutedTeal else MutedRed)) { Text("High", color = Color.White) }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onLog(sliderPosition, energyLevel) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MutedRed)
                ) {
                    Text(text = "Log", color = Color.White)
                }
            }
        }
    }
}
