package com.ilseon.drift.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.glance.appwidget.updateAll
import com.ilseon.drift.widget.OrbWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

class SleepTrackingManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("sleep_tracking_prefs", Context.MODE_PRIVATE)

    val isTracking: Boolean
        get() = prefs.getBoolean("is_tracking", false)

    fun startSleepTracking() {
        prefs.edit { putBoolean("is_tracking", true) }
        val intent = Intent(context, SleepTrackingService::class.java).apply {
            action = SleepTrackingService.ACTION_START
        }
        context.startForegroundService(intent)
        updateWidget()
    }

    fun stopSleepTracking() {
        prefs.edit { putBoolean("is_tracking", false) }
        val intent = Intent(context, SleepTrackingService::class.java)
        context.stopService(intent)

        // Reset the tracking state immediately
        SleepTrackingState.isTracking.value = false
        updateWidget()
    }

    private fun updateWidget() {
        CoroutineScope(Dispatchers.IO).launch {
            OrbWidget().updateAll(context)
        }
    }

    fun dispose() {
        // No-op
    }
}
