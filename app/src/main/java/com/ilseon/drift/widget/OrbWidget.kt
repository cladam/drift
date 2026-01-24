package com.ilseon.drift.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.service.SleepTrackingManager
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

class OrbWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            DriftRepositoryEntryPoint::class.java
        )
        val repository = hiltEntryPoint.driftRepository()
        val prefs = context.getSharedPreferences("sleep_tracking_prefs", Context.MODE_PRIVATE)
        val isSleepTracking = prefs.getBoolean("is_tracking", false)
        //val sleepTrackingManager = SleepTrackingManager(context)

        val latestCheckIn = repository.latestPulse.first()

        val moodScore = latestCheckIn?.moodScore ?: 0.5f
        val orbColor = getOrbColor(moodScore)

        provideContent {
            DriftWidgetContent(
                latestCheckIn = latestCheckIn,
                orbColor = orbColor,
                isSleepTracking = isSleepTracking
            )
        }
    }
}
