package com.ilseon.drift.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

class NotificationWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // Tillåt endast periodiska notiser mellan kl 09 och 21
        if (hour in 9..21) {
            val notificationManager = DriftNotificationManager(applicationContext)
            notificationManager.showNotification()
        }

        return Result.success()
    }
}
