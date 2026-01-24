package com.ilseon.drift

import android.app.Application
import com.ilseon.drift.notifications.DriftNotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DriftApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val notificationManager = DriftNotificationManager(this)
        notificationManager.createNotificationChannel()
    }
}
