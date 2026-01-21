package com.ilseon.drift.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey

@Composable
fun ReadinessCard(
    currentHrv: Double?,
    avgHrv: Double,
    currentSi: Double?,
    yesterdaySi: Double?,
    sleepMinutes: Int?
) {
    // Handle case where we don't have today's morning measurement yet.
    if (currentHrv == null || currentSi == null) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightGrey)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Today's Readiness", style = MaterialTheme.typography.titleMedium, color = CustomTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Measure your morning pulse to get your readiness score.", style = MaterialTheme.typography.bodyMedium, color = CustomTextSecondary)
            }
        }
        return
    }

    val readinessScore = (currentHrv / avgHrv).coerceIn(0.0, 2.0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Today's Readiness", style = MaterialTheme.typography.labelMedium, color = CustomTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))

            // Large Insight Text
            val insight = when {
                readinessScore > 1.5 -> "Your system is in peak condition."
                readinessScore > 0.9 -> "Stable balance."
                else -> "Low energy – prioritize rest."
            }
            Text(insight, style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))

            // ADHD Context / Physiological Balance
            if (currentHrv > avgHrv && currentSi < 12.0) {
                Row(
//                    modifier = Modifier
////                        .background(
////                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
////                            RoundedCornerShape(8.dp)
////                        )
//                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Physiological balance detected. Good conditions for focus today.",
                        style = MaterialTheme.typography.bodyMedium,
                        //color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // The "Why" - Passive Insights
            if (yesterdaySi != null && sleepMinutes != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val recoveryIcon = if (currentSi < yesterdaySi) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward
                    val recoveryText = if (currentSi < yesterdaySi) "dropped" else "increased"
                    Icon(imageVector = recoveryIcon, contentDescription = "Recovery Trend", modifier = Modifier.size(16.dp), tint = CustomTextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Stress has $recoveryText after ${sleepMinutes / 60}h ${sleepMinutes % 60}m of sleep.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CustomTextSecondary
                    )
                }
            }
        }
    }
}
