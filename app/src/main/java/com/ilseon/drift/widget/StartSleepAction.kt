package com.ilseon.drift.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.service.SleepTrackingManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class StartSleepAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DriftRepositoryEntryPoint::class.java
        )
        val repository = hiltEntryPoint.driftRepository()
        val sleepTrackingManager = SleepTrackingManager(context)

        val logToUpdate = repository.latestPulse.first()
        if (logToUpdate != null) {
            val updatedLog = logToUpdate.copy(
                sleepStartTime = System.currentTimeMillis(),
                sleepEndTime = null
            )
            repository.update(updatedLog)
        } else {
            val newLog = DriftLog(sleepStartTime = System.currentTimeMillis())
            repository.insert(newLog)
        }
        sleepTrackingManager.startSleepTracking()

        // After starting sleep, update the widget to reflect any changes
        OrbWidget().updateAll(context)
    }
}
