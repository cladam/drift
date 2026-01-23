package com.ilseon.drift.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.components.BalanceQuadrantCard
import com.ilseon.drift.ui.components.ReadinessCard
import com.ilseon.drift.ui.components.SleepTrendWithInsightCard
import com.ilseon.drift.ui.components.TrendSparklineCard
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.DarkGrey
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.MutedTeal
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusLow
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.viewmodels.CheckInViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    checkInViewModel: CheckInViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Trends", "Analytics")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Analytics & Trends") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkGrey,
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = DarkGrey,
                    contentColor = CustomTextSecondary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MutedTeal
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        },
        containerColor = DarkGrey,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (selectedTabIndex) {
                0 -> TrendsContent(checkInViewModel)
                1 -> AnalyticsContent(checkInViewModel)
            }
        }
    }
}

@Composable
private fun TrendsContent(checkInViewModel: CheckInViewModel) {
    val allLogs by checkInViewModel.allLogs.collectAsState()
    val latestCheckIn by checkInViewModel.latestCheckIn.collectAsState()
    val previousLog by checkInViewModel.previousLog.collectAsState()
    val latestSleepRecord by checkInViewModel.latestSleepRecord.collectAsState()
    val sevenDayHrvAverage by checkInViewModel.sevenDayHrvAverage.collectAsState()
    val yesterdayStressIndex by checkInViewModel.yesterdayEveningStressIndex.collectAsState()
    val weeklyTrend by checkInViewModel.weeklyTrend.collectAsState()

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ReadinessCard(
                currentHrv = latestCheckIn?.hrvValue,
                avgHrv = sevenDayHrvAverage,
                currentSi = latestCheckIn?.stressIndex,
                yesterdaySi = yesterdayStressIndex,
                sleepMinutes = latestSleepRecord?.sleepDurationMinutes
            )
        }

        item {
            BalanceQuadrantCard(
                hrv = latestCheckIn?.hrvValue,
                stressIndex = latestCheckIn?.stressIndex,
                previousHrv = previousLog?.hrvValue,
                previousStressIndex = previousLog?.stressIndex,
                allLogs = allLogs
            )
        }

        item {
            Text(
                "Recovery & Balance",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            TrendSparklineCard(
                title = "HRV Trend (7-day)",
                data = weeklyTrend.mapNotNull { it.hrvValue },
                higherIsBetter = true
            )
        }

        item {
            StabilityCard(weeklyTrend = weeklyTrend)
        }

        item {
            Text(
                "Physiological Load",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            TrendSparklineCard(
                title = "Stress Index Trend (7-day)",
                data = weeklyTrend.mapNotNull { it.stressIndex },
                higherIsBetter = false
            )
        }

        item {
            TrendSparklineCard(
                title = "Heartbeat Trend (7-day)",
                data = weeklyTrend.map { it.bpm?.toDouble() ?: 0.0 },
                higherIsBetter = false,
                unit = "BPM"
            )
        }

        item {
            Text(
                "Sleep & Recovery",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            SleepTrendWithInsightCard(
                weeklyTrend = weeklyTrend,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                "Emotional Wellbeing",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        item {
            TrendSparklineCard(
                title = "Mood Score (7-day)",
                data = weeklyTrend.map { (it.moodScore?.times(100))?.toDouble() ?: 0.0 },
                higherIsBetter = true,
                unit = "%"
            )
        }
    }
}

@Composable
private fun AnalyticsContent(checkInViewModel: CheckInViewModel) {
    val allLogs by checkInViewModel.allLogs.collectAsState()

    // Process data for analytics
    val measurementsByDay = allLogs
        .groupBy { SimpleDateFormat("EEEE", Locale.getDefault()).format(it.timestamp) }
        .mapValues { it.value.size }

    val hourlyCounts = allLogs
        .groupBy { Calendar.getInstance().apply { timeInMillis = it.timestamp }.get(Calendar.HOUR_OF_DAY) }
        .mapValues { it.value.size }

    val hrvByEnergy = allLogs
        .filter { it.energyLevel != null }
        .groupBy { it.energyLevel!! }
        .mapValues { (_, logs) ->
            logs.mapNotNull { it.hrvValue }.average().takeIf { !it.isNaN() }
        }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            val measurementsThisMonth = allLogs.count {
                val cal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                val currentCal = Calendar.getInstance()
                cal.get(Calendar.MONTH) == currentCal.get(Calendar.MONTH) &&
                        cal.get(Calendar.YEAR) == currentCal.get(Calendar.YEAR)
            }
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().time).lowercase()

            Card(colors = CardDefaults.cardColors(containerColor = LightGrey)) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = buildAnnotatedString {
                            append("You've checked in with yourself ")
                            withStyle(SpanStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize, fontWeight = FontWeight.Bold)) {
                                append("$measurementsThisMonth")
                            }
                            append(" times — that's ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                append("$measurementsThisMonth")
                            }
                            append(" moments of mindfulness this $monthName.")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = CustomTextSecondary
                    )
                }
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = LightGrey)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Check-ins by Day", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                    daysOfWeek.forEach { day ->
                        val count = measurementsByDay[day] ?: 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(day, Modifier.weight(1f))
                            Text(count.toString())
                        }
                    }
                }
            }
        }

        item {
            HrvByEnergyCard(hrvByEnergy = hrvByEnergy)
        }

        item {
            HourlyActivityChart(hourlyCounts = hourlyCounts)
        }
    }
}

@Composable
private fun HourlyActivityChart(hourlyCounts: Map<Int, Int>) {
    Card(colors = CardDefaults.cardColors(containerColor = LightGrey)) {
        Column(Modifier.padding(16.dp)) {
            Text("Activity by Hour", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))

            val maxCount = hourlyCounts.values.maxOrNull() ?: 1

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                (0..23).forEach { hour ->
                    val count = hourlyCounts[hour] ?: 0
                    val barHeight = (count.toFloat() / maxCount.toFloat() * 100).coerceAtLeast(0f)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(barHeight.dp)
                                .fillMaxWidth(0.6f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MutedTeal)
                        )
                        Text("%02d".format(hour), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun HrvByEnergyCard(hrvByEnergy: Map<String, Double?>) {
    Card(colors = CardDefaults.cardColors(containerColor = LightGrey)) {
        Column(Modifier.padding(16.dp)) {
            Text("HRV by Energy Level", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            val energyLevels = listOf("HIGH", "MEDIUM", "LOW")
            energyLevels.forEach { level ->
                val avgHrv = hrvByEnergy[level]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        level.lowercase().replaceFirstChar { it.uppercase() },
                        Modifier.weight(1f)
                    )
                    Text(
                        text = avgHrv?.let { "%.1f".format(it) } ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun StabilityCard(weeklyTrend: List<DriftLog>) {
    val hrvValues = weeklyTrend.mapNotNull { it.hrvValue }

    val cv: Double? = if (hrvValues.size > 1) {
        val mean = hrvValues.average()
        val stdDev = sqrt(hrvValues.map { (it - mean) * (it - mean) }.average())
        if (mean > 0) (stdDev / mean) * 100 else 0.0
    } else {
        null
    }

    val (stabilityInfo, statusColor) = when {
        cv == null -> Triple("N/A", "Not Enough Data", "Need at least two measurements this week to calculate stability.") to CustomTextSecondary
        cv < 10 -> Triple("%.1f%%".format(cv), "Very Stable", "Your nervous system is showing strong resilience and stability.") to StatusLow
        cv < 15 -> Triple("%.1f%%".format(cv), "Stable", "Your system is maintaining good balance day-to-day.") to StatusMedium
        else -> Triple("%.1f%%".format(cv), "Variable", "Your system is working hard to adapt. Consider focusing on consistent routines.\nA variable system often needs lower sensory input. Consider a 'low-friction' day.") to StatusHigh
    }
    val (stabilityValue, stabilityStatus, stabilityDescription) = stabilityInfo

    Card(colors = CardDefaults.cardColors(containerColor = LightGrey)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text("System Stability", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    text = stabilityValue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(stabilityStatus, color = statusColor, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = stabilityDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = CustomTextSecondary
            )
        }
    }
}
