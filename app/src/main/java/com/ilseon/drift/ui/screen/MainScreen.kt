package com.ilseon.drift.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.components.CheckInModal
import com.ilseon.drift.ui.components.EnergyOrb
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.DarkGrey
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusMedium

@Composable
fun MainScreen() {
    var showCheckInModal by remember { mutableStateOf(false) }

    if (showCheckInModal) {
        CheckInModal(
            onDismissRequest = { showCheckInModal = false },
            onLog = {
                // TODO: Save the mood to the database
                showCheckInModal = false
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkGrey
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showCheckInModal = true },
                contentAlignment = Alignment.Center
            ) {
                EnergyOrb(targetColor = StatusMedium)
            }

            // Data cards at the bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                DataCard(title = "Last Sleep", value = "8h 15m")
                Spacer(modifier = Modifier.height(16.dp))
                DataCard(title = "HRV", value = "62ms", icon = Icons.Default.Favorite)
            }
        }
    }
}

@Composable
fun DataCard(title: String, value: String, icon: ImageVector? = null) {
    Card(
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
