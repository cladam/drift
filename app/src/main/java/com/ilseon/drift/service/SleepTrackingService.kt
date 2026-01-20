package com.ilseon.drift.service

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.ilseon.drift.notifications.DriftNotificationManager
import kotlin.compareTo

class SleepTrackingService : Service() {

    private lateinit var notificationManager: DriftNotificationManager
    private var unlockReceiver: SleepUnlockReceiver? = null
    private var sleepStartTime: Long = 0L

    companion object {
        private const val TAG = "SleepTrackingService"
        const val ACTION_START = "com.ilseon.drift.service.action.START_SLEEP_TRACKING"
        const val ACTION_STOP = "com.ilseon.drift.service.action.STOP_SLEEP_TRACKING"
        const val ACTION_UNLOCK_DETECTED = "com.ilseon.drift.service.action.UNLOCK_DETECTED"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = DriftNotificationManager(this)
        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
            ACTION_UNLOCK_DETECTED -> handleUnlock()
        }
        return START_STICKY
    }

    private fun startTracking() {
        try {
            Log.d(TAG, "startTracking called")
            sleepStartTime = System.currentTimeMillis()
            val notification = notificationManager.getSleepTrackingNotification()
            Log.d(TAG, "Notification built, calling startForeground")
            startForeground(DriftNotificationManager.SLEEP_NOTIFICATION_ID, notification)
            Log.d(TAG, "startForeground succeeded")

            unlockReceiver = SleepUnlockReceiver()
            val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
            ContextCompat.registerReceiver(this, unlockReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
            Log.d(TAG, "Unlock receiver registered")

            SleepTrackingState.isTracking.value = true
            Log.d(TAG, "Sleep tracking started.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start tracking: ${e.message}", e)
        }
    }


    private fun handleUnlock() {
        Log.d(TAG, "Unlock event received by service.")
        if (sleepStartTime == 0L) {
            Log.w(TAG, "Unlock received but tracking was not started properly.")
            return
        }
        val elapsedMinutes = (System.currentTimeMillis() - sleepStartTime) / 60000
        Log.d(TAG, "Elapsed time: $elapsedMinutes minute(s).")

        if (elapsedMinutes >= 1) {
            Log.d(TAG, "Threshold met. Stopping tracking.")
            stopTracking()
        } else {
            Log.d(TAG, "Threshold not met. Continuing to track.")
        }
    }

    private fun stopTracking() {
        Log.d(TAG, "stopTracking called")
        unlockReceiver?.let {
            unregisterReceiver(it)
            unlockReceiver = null
            Log.d(TAG, "Unlock receiver unregistered")
        }

        // Store the sleep duration before stopping
        val sleepDurationMs = if (sleepStartTime > 0) System.currentTimeMillis() - sleepStartTime else 0L
        SleepTrackingState.sleepEndTime.value = System.currentTimeMillis()
        SleepTrackingState.sleepDurationMs.value = sleepDurationMs

        stopForeground(STOP_FOREGROUND_REMOVE)
        SleepTrackingState.isTracking.value = false
        stopSelf()
        Log.d(TAG, "Service stopped. Duration: ${sleepDurationMs / 60000} minutes")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Service destroyed.")
        unlockReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w(TAG, "Receiver already unregistered")
            }
            unlockReceiver = null
        }

        // Ensure state is updated if destroyed externally
        if (SleepTrackingState.isTracking.value) {
            val sleepDurationMs = if (sleepStartTime > 0) System.currentTimeMillis() - sleepStartTime else 0L
            SleepTrackingState.sleepEndTime.value = System.currentTimeMillis()
            SleepTrackingState.sleepDurationMs.value = sleepDurationMs
            SleepTrackingState.isTracking.value = false
        }

        super.onDestroy()
    }
}
