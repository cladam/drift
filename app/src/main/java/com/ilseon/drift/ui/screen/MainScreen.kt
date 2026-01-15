package com.ilseon.drift.ui.screen

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
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.components.CheckInModal
import com.ilseon.drift.ui.components.EnergyOrb
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.DarkGrey
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedDetail
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent
import com.ilseon.drift.ui.viewmodels.CheckInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(checkInViewModel: CheckInViewModel) {
    val latestCheckIn: DriftLog? by checkInViewModel.latestCheckIn.collectAsState()
    var showCheckInModal by remember { mutableStateOf(false) }
    val orbColor = latestCheckIn?.moodScore?.let { lerp(StatusUrgent, StatusMedium, it) } ?: StatusMedium

    if (showCheckInModal) {
        CheckInModal(
            onDismissRequest = { showCheckInModal = false },
            onLog = { sliderValue, energyLevel ->
                checkInViewModel.insert(sliderValue, energyLevel)
                showCheckInModal = false
            },
            latestCheckIn = latestCheckIn,
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
                    DataCard(title = "Last Sleep", value = "8h 15m", icon = Icons.Default.Bed, modifier = Modifier.weight(1f))
                    DataCard(title = "HRV", value = "62ms", icon = Icons.Default.Favorite, modifier = Modifier.weight(1f))
                }
                DataCard(
                    title = "Analytics",
                    value = "7d trend",
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    modifier = Modifier.fillMaxWidth()
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
                    .clickable { showCheckInModal = true },
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

@Composable
fun DataCard(title: String, value: String, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp),
                    tint = MutedTeal
                )
                Spacer(modifier = Modifier.size(16.dp))
            }
            Column {
                Text(text = title, color = CustomTextSecondary)
                Text(text = value, color = CustomTextPrimary)
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
