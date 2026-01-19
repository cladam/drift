package com.ilseon.drift.service

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.ilseon.drift.notifications.DriftNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SleepTrackingManager(private val context: Context) {

    private val _sleepTrackingActive = MutableStateFlow(false)
    val sleepTrackingActive: StateFlow<Boolean> = _sleepTrackingActive

    private var unlockReceiver: SleepUnlockReceiver? = null
    private var sleepStartTime: Long = 0L
    private val notificationManager = DriftNotificationManager(context)

    fun startSleepTracking() {
        if (_sleepTrackingActive.value) return

        sleepStartTime = System.currentTimeMillis()
        _sleepTrackingActive.value = true

        notificationManager.showSleepTrackingNotification()

        unlockReceiver = SleepUnlockReceiver {
            // Wait 2 hours before we start counting sleep, for bedtime scrolling - buh
            val elapsedHours = (System.currentTimeMillis() - sleepStartTime) / 3600000
            if (elapsedHours >= 2) {
                stopSleepTracking()
            }

            // For testing, stop tracking after 1 minute. In production, this should be a longer duration like 2 hours.
//            val elapsedMinutes = (System.currentTimeMillis() - sleepStartTime) / 60000
//            if (elapsedMinutes >= 1) {
//                stopSleepTracking()
//            }
        }

        context.registerReceiver(
            unlockReceiver,
            IntentFilter(Intent.ACTION_USER_PRESENT)
        )
    }

    fun stopSleepTracking() {
        if (!_sleepTrackingActive.value) return

        _sleepTrackingActive.value = false
        notificationManager.dismissSleepTrackingNotification()
        unlockReceiver?.let {
            context.unregisterReceiver(it)
            unlockReceiver = null
        }
    }
}
