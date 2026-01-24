package com.ilseon.drift.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import kotlin.math.ln

@Composable
fun ReadinessCard(
    currentHrv: Double?,
    sevenDayHrvAverage: Double,
    sixtyDayHrvAverage: Double,
    hrvCv: Double?,
    currentSi: Double?,
    yesterdaySi: Double?,
    sleepMinutes: Int?,
    fourteenDaySleepAverage: Double?
) {
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

    val readinessScore = calculateReadinessScore(
        currentHrv = currentHrv,
        sevenDayHrvAverage = sevenDayHrvAverage,
        hrvCv = hrvCv,
        sleepMinutes = sleepMinutes,
        fourteenDaySleepAverage = fourteenDaySleepAverage
    )

    val readinessColor = when {
        readinessScore > 85 -> MutedTeal
        readinessScore in 70..84 -> CustomTextSecondary
        else -> StatusHigh
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightGrey)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            ReadinessScore(readinessScore, readinessColor)
            Spacer(modifier = Modifier.height(16.dp))
            ContextualInsight(readinessScore, currentHrv, sevenDayHrvAverage, currentSi, yesterdaySi, sleepMinutes)
        }
    }
}

private fun calculateReadinessScore(
    currentHrv: Double,
    sevenDayHrvAverage: Double,
    hrvCv: Double?,
    sleepMinutes: Int?,
    fourteenDaySleepAverage: Double?
): Int {
    // 1. HRV Score (log-scaled)
    val hrvRatio = (ln(currentHrv) / ln(sevenDayHrvAverage)).coerceIn(0.0, 1.2)
    var hrvScore = hrvRatio * 100

    // Apply Stability Adjustment if we have CV data
    hrvCv?.let { cv ->
        val stabilityMultiplier = when {
            cv < 5.0 -> 1.05  // 5% bonus for high stability
            cv > 12.0 -> 0.90 // 10% penalty for wild swings
            else -> 1.0
        }
        hrvScore *= stabilityMultiplier
    }

    // 2. Sleep Score
    val sleepTarget = fourteenDaySleepAverage ?: 480.0 // Default to 8 hours if no average
    val sleepScore = ((sleepMinutes?.toDouble() ?: 0.0) / sleepTarget * 100).coerceIn(50.0, 110.0)

    // 3. Combine scores
    val combinedScore = (hrvScore * 0.6 + sleepScore * 0.4)

    return combinedScore.toInt().coerceIn(0, 100)
}

@Composable
private fun ReadinessScore(score: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Readiness Score", style = MaterialTheme.typography.labelMedium, color = CustomTextSecondary)
//        Spacer(Modifier.width(4.dp))
//        HelpIconWithModal(
//            title = "About Your Readiness Score",
//            content = {
//                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Text(
//                        "Your Readiness Score is a holistic measure of your body's recovery and preparedness for the day.",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Text(
//                        "It's calculated based on:",
//                        style = MaterialTheme.typography.bodyMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        "• HRV Balance: Your current HRV compared to your long-term baseline. It shows your physiological stress.",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Text(
//                        "• Sleep Balance: Your recent sleep duration compared to your own average. It reflects your sleep consistency.",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Text(
//                        "• System Stability: The day-to-day variation of your HRV. High stability (low variation) is a sign of resilience.",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//        )
        Text(score.toString(), style = MaterialTheme.typography.displayLarge, color = color)
    }
}

@Composable
private fun ContextualInsight(
    readinessScore: Int,
    currentHrv: Double,
    avgHrv: Double,
    currentSi: Double,
    yesterdaySi: Double?,
    sleepMinutes: Int?
) {
    val insight = when {
        readinessScore > 85 -> "Optimal. You are ready for deep focus."
        readinessScore in 70..84 -> "Good. Stable. Stick to your routines."
        else -> "Pay Attention. System under strain. Consider a low-friction day."
    }
    Text(insight, style = MaterialTheme.typography.bodyLarge)

    Spacer(modifier = Modifier.height(16.dp))

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
