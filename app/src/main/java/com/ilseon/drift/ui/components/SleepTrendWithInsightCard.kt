package com.ilseon.drift.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.theme.MutedDetail
import kotlin.compareTo
import kotlin.text.toDouble
import kotlin.text.toInt
import kotlin.times
import kotlin.unaryMinus

@Composable
fun SleepTrendWithInsightCard(
    weeklyTrend: List<DriftLog>,
    modifier: Modifier = Modifier
) {
    val validSleepEntries = weeklyTrend.filter { (it.sleepDurationMinutes ?: 0) > 0 }
    val sleepData = validSleepEntries.mapNotNull { it.sleepDurationMinutes?.toDouble() }

    if (sleepData.size < 2) return

    val avgSleepMinutes = sleepData.average()
    val latestSleep = sleepData.last()
    val deviation = latestSleep - avgSleepMinutes

    val avgMoodOnGoodSleep = validSleepEntries
        .filter { it.sleepDurationMinutes!! >= 420 }
        .map { it.moodScore?.toDouble() ?: 0.0 }
        .takeIf { it.isNotEmpty() }
        ?.average()

    val avgMoodOnPoorSleep = validSleepEntries
        .filter { it.sleepDurationMinutes!! < 420 }
        .map { it.moodScore?.toDouble() ?: 0.0 }
        .takeIf { it.isNotEmpty() }
        ?.average()

    Column(modifier = modifier) {
        TrendSparklineCard(
            title = "Sleep Duration (7-day avg: ${formatDuration(avgSleepMinutes.toInt())})",
            data = sleepData,
            higherIsBetter = true,
            unit = "",
            formatValue = { formatDuration(it.toInt()) }
        )

        if (avgMoodOnGoodSleep != null && avgMoodOnPoorSleep != null) {
            val moodDiff = ((avgMoodOnGoodSleep - avgMoodOnPoorSleep) * 100).toInt()
            val insight = when {
                moodDiff > 5 -> "💡 Mood is ${moodDiff}% higher after 7+ hours of sleep"
                moodDiff < -5 -> "💡 Shorter sleep correlates with better mood"
                else -> "💡 Sleep duration doesn't affect your mood much"
            }
            Text(
                text = insight,
                style = MaterialTheme.typography.bodySmall,
                color = MutedDetail,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp)
            )
        }
    }
}

private fun formatDuration(minutes: Int): String {
    val hours = kotlin.math.abs(minutes) / 60
    val mins = kotlin.math.abs(minutes) % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        else -> "${hours}h ${mins}m"
    }
}