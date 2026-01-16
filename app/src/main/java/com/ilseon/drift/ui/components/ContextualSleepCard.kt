package com.ilseon.drift.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.theme.BorderQuiet
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent

@Composable
fun ContextualSleepCard(
    sleepMinutes: Int?,
    onLogClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onLogClick() },
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (sleepMinutes != null) {
                Text("Last Sleep", color = CustomTextSecondary)
                Text("${sleepMinutes / 60}h ${sleepMinutes % 60}m", color = CustomTextPrimary) //
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Bedtime, contentDescription = null, tint = MutedTeal) //
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "No sleep data found. Tap to log.",
                        color = CustomTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}