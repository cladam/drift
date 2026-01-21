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
import com.ilseon.drift.ui.components.StressGauge
import com.ilseon.drift.ui.components.calculateBpmFromIntervals
import com.ilseon.drift.ui.components.calculateRmssdFromIntervals
import com.ilseon.drift.ui.components.calculateStressIndex
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
    onCheckInHandled: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val latestCheckIn: DriftLog? by checkInViewModel.latestCheckIn.collectAsState()
    val latestSleepRecord: DriftLog? by checkInViewModel.latestSleepRecord.collectAsState()
    var showCheckInModal by remember { mutableStateOf(false) }
    var newHrvValue by remember { mutableStateOf<Double?>(null) }
    var bpmValue by remember { mutableStateOf<Int?>(null) }
    var stressIndex by remember { mutableStateOf<Double?>(null) }
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

    // --- Holistic Orb Color Calculation ---
    val moodScore = latestCheckIn?.moodScore ?: 0.5f
    val energyLevel = latestCheckIn?.energyLevel
    val stress = stressIndex ?: latestCheckIn?.stressIndex

    // 1. Normalize Scores (0.0 - 1.0, where 1.0 is positive)
    val mood = moodScore
    val energy = when (energyLevel) {
        "HIGH" -> 1.0f
        "MEDIUM" -> 0.5f
        "LOW" -> 0.0f
        else -> 0.5f // Default to medium if not set
    }
    val stressNormalized = stress?.let {
        // Invert stress: lower is better. Max out at a reasonable 25 for scaling.
        (1.0f - (it / 25.0).toFloat()).coerceIn(0.0f, 1.0f)
    } ?: 0.5f // Default to neutral if no stress data

    // 2. Weighted Average
    val combinedScore = (mood * 0.5f) + (energy * 0.25f) + (stressNormalized * 0.25f)

    // 3. Determine Color based on the combined score
    val orbColor = if (combinedScore > 0.5f) {
        lerp(StatusMedium, StatusHigh, (combinedScore - 0.5f) * 2)
    } else {
        lerp(StatusUrgent, StatusMedium, combinedScore * 2)
    }

    var firstPulseTime by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(firstPulseTime) {
        if (isMeasuringHrv && firstPulseTime != null) {
            delay(40000)

            val timestamps = pulseTimestamps.toList()
            val pulseCount = timestamps.size

            if (pulseCount > 10) {
                val stablePulses = timestamps.drop(5)
                val intervals = stablePulses.zipWithNext { a, b -> b - a }
                val validIntervals = intervals.filter { it in 550L..1200L }
                Log.d("PulseAnalyzer HRV", "Stable pulses: ${stablePulses.size}")
                Log.d("PulseAnalyzer HRV", "Valid intervals: ${validIntervals.size}")

                if (validIntervals.size >= 10) {
                    newHrvValue = calculateRmssdFromIntervals(validIntervals)
                    bpmValue = calculateBpmFromIntervals(validIntervals)
                    stressIndex = calculateStressIndex(validIntervals)
                    showCheckInModal = true

                    // Log data
                    Log.d("PulseAnalyzer HRV", "New HRV: $newHrvValue")
                    Log.d("PulseAnalyzer HRV", "New BPM: $bpmValue")
                    Log.d("PulseAnalyzer HRV", "New Stress: $stressIndex")
                } else {
                    Log.d("PulseAnalyzer HRV", "Not enough valid intervals: ${validIntervals.size}")
                }
            } else {
                Log.d("PulseAnalyzer HRV", "Not enough pulses: $pulseCount")
            }

            isMeasuringHrv = false
        }
    }

    LaunchedEffect(isMeasuringHrv) {
        if (isMeasuringHrv) {
            pulseTimestamps.clear()
            firstPulseTime = null
            newHrvValue = null
            stressIndex = null
        }
    }

    if (showCheckInModal) {
        CheckInModal(
            onDismissRequest = { 
                showCheckInModal = false
            },
            onLog = { sliderValue, energyLvl, hrv, bpm, stress ->
                checkInViewModel.insert(sliderValue, energyLvl, hrv, bpm, stress)
                showCheckInModal = false
            },
            latestCheckIn = latestCheckIn,
            hrv = newHrvValue,
            bpm = bpmValue,
            stressIndex = stressIndex
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
                EnergyOrb(
                    targetColor = orbColor,
                    isMeasuring = isMeasuringHrv,
                    hrv = (newHrvValue ?: latestCheckIn?.hrvValue)?.toFloat(),
                    stressIndex = (stressIndex ?: latestCheckIn?.stressIndex)?.toFloat(),
                    energy = energy
                )
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
                            }
                        }
                    )
                }
                StressGauge(
                    stressIndex = stressIndex ?: latestCheckIn?.stressIndex,
                    moodScore = moodScore,
                    modifier = Modifier.fillMaxWidth()
                )
                AnalyticsCard(
                    title = "Analytics",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    modifier = Modifier.fillMaxWidth(),
                    logs = weeklyTrend,
                    onClick = onNavigateToAnalytics
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
