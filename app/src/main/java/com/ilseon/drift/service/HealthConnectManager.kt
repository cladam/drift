package com.ilseon.drift.service

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    suspend fun fetchYesterdaySleep(): Long? {
        val startTime = Instant.now().minus(Duration.ofDays(1))
        val endTime = Instant.now()

        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        // Sum up the duration of sleep stages or sessions
        return response.records.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes()
        }
    }

    suspend fun fetchLatestHRV(): Double? {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.now().minus(Duration.ofDays(1)), Instant.now()
                ),
                ascendingOrder = false,
                pageSize = 1
            )
        )
        return response.records.firstOrNull()?.heartRateVariabilityMillis
    }
}