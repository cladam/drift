package com.ilseon.drift.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ilseon.drift.ui.components.ReadinessCard
import com.ilseon.drift.ui.components.TrendSparklineCard
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.DarkGrey
import com.ilseon.drift.ui.viewmodels.CheckInViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    checkInViewModel: CheckInViewModel,
    onNavigateBack: () -> Unit
) {
    val allLogs by checkInViewModel.allLogs.collectAsState()
    val latestCheckIn by checkInViewModel.latestCheckIn.collectAsState()
    val latestSleepRecord by checkInViewModel.latestSleepRecord.collectAsState()
    val sevenDayHrvAverage by checkInViewModel.sevenDayHrvAverage.collectAsState()
    val yesterdayStressIndex by checkInViewModel.yesterdayEveningStressIndex.collectAsState()
    val weeklyTrend by checkInViewModel.weeklyTrend.collectAsState()

    Scaffold(
        topBar = {
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
        },
        containerColor = DarkGrey,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
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
                TrendSparklineCard(
                    title = "HRV Trend (7-day)",
                    data = weeklyTrend.mapNotNull { it.hrvValue }
                )
            }

            item {
                TrendSparklineCard(
                    title = "Stress Index Trend (7-day)",
                    data = weeklyTrend.mapNotNull { it.stressIndex }
                )
            }

            item {
                Text(
                    "All Time Data",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            items(allLogs) { log ->
                Text(log.toString(), color = CustomTextSecondary)
            }
        }
    }
}
