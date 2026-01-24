package com.ilseon.drift.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.service.SleepTrackingManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class EndSleepAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DriftRepositoryEntryPoint::class.java
        )
        val repository = hiltEntryPoint.driftRepository()
        val sleepTrackingManager = SleepTrackingManager(context)

        sleepTrackingManager.stopSleepTracking()

        val logToUpdate = repository.latestPulse.first()
        if (logToUpdate != null && logToUpdate.sleepStartTime != null && logToUpdate.sleepEndTime == null) {
            val sleepEndTime = System.currentTimeMillis()
            val sleepStartTime = logToUpdate.sleepStartTime!!
            val sleepDuration = sleepEndTime - sleepStartTime
            val sleepDurationMinutes = (sleepDuration / (1000 * 60)).toInt()

            val updatedLog = logToUpdate.copy(
                sleepEndTime = sleepEndTime,
                sleepDurationMinutes = sleepDurationMinutes
            )
            repository.update(updatedLog)
        }

        // After stopping sleep, update the widget to reflect any changes
        OrbWidget().updateAll(context)
    }
}
