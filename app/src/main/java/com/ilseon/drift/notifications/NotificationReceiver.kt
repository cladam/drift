package com.ilseon.drift.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class NotificationReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = DriftNotificationManager(context)
        notificationManager.showNotification()
        // Reschedule for the next day
        notificationManager.scheduleMorningNotification()
    }
}
