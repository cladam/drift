package com.ilseon.drift.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal

@Composable
fun ContextualSleepCard(
    sleepMinutes: Int?,
    isSleeping: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = LightGrey),
        border = if (isSleeping) BorderStroke(1.dp, MutedTeal) else null
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bedtime, contentDescription = null, tint = MutedTeal)
            Spacer(Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center) {
                Text("Sleep", color = CustomTextSecondary)
                when {
                    isSleeping -> {
                        Text("Tracking...", color = MutedTeal, style = MaterialTheme.typography.bodyLarge)
                    }
                    sleepMinutes != null -> {
                        Text("${sleepMinutes / 60}h ${sleepMinutes % 60}m", color = CustomTextPrimary)
                    }
                    else -> {
                        Text("Tap to sleep", color = CustomTextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
