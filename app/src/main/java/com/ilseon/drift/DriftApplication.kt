package com.ilseon.drift

import android.app.Application
import com.ilseon.drift.data.DriftDatabase
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.notifications.DriftNotificationManager

class DriftApplication : Application() {
    val database by lazy { DriftDatabase.getDatabase(this) }
    val repository by lazy { DriftRepository(database.driftDao()) }

    override fun onCreate() {
        super.onCreate()
        val notificationManager = DriftNotificationManager(this)
        notificationManager.createNotificationChannel()
    }
}
