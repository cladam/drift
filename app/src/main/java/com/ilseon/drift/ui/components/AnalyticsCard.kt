package com.ilseon.drift.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    logs: List<DriftLog>
) {
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
        WeekStrip(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            logs = logs
        )
    }
}

@Composable
fun WeekStrip(
    modifier: Modifier = Modifier,
    logs: List<DriftLog>
) {
    val logsByDay = logs.groupBy {
        val cal = Calendar.getInstance()
        cal.timeInMillis = it.timestamp
        cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }

    val avgMoodByDay = logsByDay.mapValues { entry ->
        entry.value.map { it.moodScore }.average()
    }

    val lastLogByDay = logsByDay.mapValues { entry ->
        entry.value.maxByOrNull { it.timestamp }
    }

    val days = (0..6).map {
        val dayCal = Calendar.getInstance()
        dayCal.add(Calendar.DAY_OF_YEAR, -6 + it)
        dayCal
    }

    val dayFormatter = SimpleDateFormat("E", Locale.getDefault())
    val today = Calendar.getInstance()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { dayCal ->
            val dayKey = dayCal.get(Calendar.YEAR) * 1000 + dayCal.get(Calendar.DAY_OF_YEAR)
            val isToday = dayCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    dayCal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)

            // --- Background color from average mood ---
            val avgMood = avgMoodByDay[dayKey]
            val avgColor = when {
                avgMood != null -> {
                    if (avgMood > 0.5f) {
                        lerp(StatusMedium, StatusHigh, ((avgMood - 0.5f) * 2).toFloat())
                    } else {
                        lerp(StatusUrgent, StatusMedium, (avgMood * 2).toFloat())
                    }
                }
                else -> Color.Transparent
            }

            // --- Border color from last mood of today ---
            val lastLog = lastLogByDay[dayKey]
            var border = BorderStroke(1.dp, BorderQuiet)
            if (isToday && lastLog != null) {
                val lastMoodColor = if (lastLog.moodScore > 0.5f) {
                    lerp(StatusMedium, StatusHigh, ((lastLog.moodScore - 0.5f) * 2).toFloat())
                } else {
                    lerp(StatusUrgent, StatusMedium, (lastLog.moodScore * 2).toFloat())
                }
                border = BorderStroke(2.dp, lastMoodColor)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(avgColor, shape = RoundedCornerShape(4.dp))
                        .border(border, shape = RoundedCornerShape(4.dp))
                )
                Text(
                    text = dayFormatter.format(dayCal.time).first().toString(),
                    color = CustomTextSecondary,
                    fontSize = 12.sp
                )
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
