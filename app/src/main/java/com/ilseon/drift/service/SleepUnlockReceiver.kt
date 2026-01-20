package com.ilseon.drift.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class SleepUnlockReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SleepUnlockReceiver"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d(TAG, "onReceive called with action: ${intent?.action}")

        if (intent?.action == Intent.ACTION_USER_PRESENT) {
            Log.d(TAG, "USER_PRESENT received, forwarding to service")

            val serviceIntent = Intent(context, SleepTrackingService::class.java).apply {
                action = SleepTrackingService.ACTION_UNLOCK_DETECTED
            }

            try {
                context.startForegroundService(serviceIntent)
                Log.d(TAG, "Service intent sent successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start service: ${e.message}")
            }
        }
    }
}