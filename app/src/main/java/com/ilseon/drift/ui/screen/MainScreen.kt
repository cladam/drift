package com.ilseon.drift.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.components.AnalyticsCard
import com.ilseon.drift.ui.components.CheckInModal
import com.ilseon.drift.ui.components.ContextualPulseCard
import com.ilseon.drift.ui.components.ContextualSleepCard
import com.ilseon.drift.ui.components.EnergyOrb
import com.ilseon.drift.ui.components.calculateBpmFromIntervals
import com.ilseon.drift.ui.components.calculateRmssdFromIntervals
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.DarkGrey
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedDetail
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent
import com.ilseon.drift.ui.viewmodels.CheckInViewModel
import kotlinx.coroutines.delay
import kotlin.text.clear

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    checkInViewModel: CheckInViewModel,
    showCheckInFromNotification: Boolean,
    onCheckInHandled: () -> Unit
) {
    val latestCheckIn: DriftLog? by checkInViewModel.latestCheckIn.collectAsState()
    val latestSleepRecord: DriftLog? by checkInViewModel.latestSleepRecord.collectAsState()
    var showCheckInModal by remember { mutableStateOf(false) }
    var newHrvValue by remember { mutableStateOf<Double?>(null) }
    var bpmValue by remember { mutableStateOf<Int?>(null) }
    val weeklyTrend by checkInViewModel.weeklyTrend.collectAsState()
    val isSleeping = latestCheckIn?.sleepStartTime != null && latestCheckIn?.sleepEndTime == null
    var isMeasuringHrv by remember { mutableStateOf(false) }
    val pulseTimestamps = remember { mutableStateListOf<Long>() }
    val context = LocalContext.current

    if (showCheckInFromNotification) {
        showCheckInModal = true
        onCheckInHandled()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            isMeasuringHrv = true
        }
    }

    val moodScore = latestCheckIn?.moodScore ?: 0.5f
    val orbColor = if (moodScore > 0.5f) {
        lerp(StatusMedium, StatusHigh, (moodScore - 0.5f) * 2)
    } else {
        lerp(StatusUrgent, StatusMedium, moodScore * 2)
    }
    var firstPulseTime by remember { mutableStateOf<Long?>(null) }
    var stablePulseStartIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(firstPulseTime) {
        if (isMeasuringHrv && firstPulseTime != null) {
            delay(20000)

            val timestamps = pulseTimestamps.toList()
            val pulseCount = timestamps.size

            if (pulseCount > 5) {
                val stablePulses = timestamps.drop(1)
                val intervals = stablePulses.zipWithNext { a, b -> b - a }
                val validIntervals = intervals.filter { it in 550L..1200L }

                if (validIntervals.size >= 4) {
                    newHrvValue = calculateRmssdFromIntervals(validIntervals)
                    bpmValue = calculateBpmFromIntervals(validIntervals)
                    showCheckInModal = true
                } else {
                    Log.d("HRV", "Not enough valid intervals: ${validIntervals.size}")
                }
            } else {
                Log.d("HRV", "Not enough pulses: $pulseCount")
            }

            isMeasuringHrv = false
        }
    }

    LaunchedEffect(isMeasuringHrv) {
        if (isMeasuringHrv) {
            pulseTimestamps.clear()
            firstPulseTime = null
            stablePulseStartIndex = 0
            newHrvValue = null
        }
    }


    if (showCheckInModal) {
        CheckInModal(
            onDismissRequest = { 
                showCheckInModal = false
            },
            onLog = { sliderValue, energyLevel, hrv, bpm ->
                checkInViewModel.insert(sliderValue, energyLevel, hrv, bpm)
                showCheckInModal = false
            },
            latestCheckIn = latestCheckIn,
            hrv = newHrvValue,
            bpm = bpmValue
        )
    }

    Scaffold(
        containerColor = DarkGrey,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EnergyOrb(targetColor = orbColor)
            }

            // Data cards
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ContextualSleepCard(
                        sleepMinutes = latestSleepRecord?.sleepDurationMinutes,
                        isSleeping = isSleeping,
                        onClick = {
                            if (isSleeping) {
                                checkInViewModel.endSleep()
                            } else {
                                checkInViewModel.startSleep()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    ContextualPulseCard(
                        hrvValue = newHrvValue ?: latestCheckIn?.hrvValue,
                        bpmValue = bpmValue ?: latestCheckIn?.bpm,
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> {
                                    isMeasuringHrv = true
                                }

                                else -> {
                                    launcher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        },
                        onCancel = {
                            isMeasuringHrv = false
                            pulseTimestamps.clear()
                            firstPulseTime = null
                        },
                        modifier = Modifier.weight(1f),
                        isMeasuring = isMeasuringHrv,
                        onPulse = { timestamp ->
                            pulseTimestamps.add(timestamp)
                            if (firstPulseTime == null) {
                                firstPulseTime = timestamp
                                stablePulseStartIndex = 0
                            }
                        }
                    )
                }
                AnalyticsCard(
                    title = "Analytics",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    modifier = Modifier.fillMaxWidth(),
                    logs = weeklyTrend
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Centered FAB
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LightGrey, CircleShape)
                    .border(2.dp, MutedDetail, CircleShape)
                    .clickable { 
                        newHrvValue = null
                        showCheckInModal = true 
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Check-in",
                        modifier = Modifier.size(36.dp),
                        tint = CustomTextPrimary
                    )
                }
            }
        }
    }
}

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    val red = (start.red * (1 - fraction) + stop.red * fraction)
    val green = (start.green * (1 - fraction) + stop.green * fraction)
    val blue = (start.blue * (1 - fraction) + stop.blue * fraction)
    val alpha = (start.alpha * (1 - fraction) + stop.alpha * fraction)
    return Color(red, green, blue, alpha)
}
