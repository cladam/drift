package com.ilseon.drift.service

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

class SleepTrackingManager(private val context: Context) {

    val sleepTrackingActive: StateFlow<Boolean> = SleepTrackingState.isTracking

    fun startSleepTracking() {
        val intent = Intent(context, SleepTrackingService::class.java).apply {
            action = SleepTrackingService.ACTION_START
        }
        context.startForegroundService(intent)
    }

    fun stopSleepTracking() {
        // Use stopService() directly instead of sending an intent
        // This avoids the startForeground() requirement
        val intent = Intent(context, SleepTrackingService::class.java)
        context.stopService(intent)

        // Reset the tracking state immediately
        SleepTrackingState.isTracking.value = false
    }

    fun dispose() {
        // No-op
    }
}
