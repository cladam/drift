package com.ilseon.drift.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import com.ilseon.drift.data.DriftDatabase
import com.ilseon.drift.data.DriftRepository
import kotlinx.coroutines.flow.first

class OrbWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DriftRepository(DriftDatabase.getDatabase(context).driftDao())
        val latestCheckIn = repository.latestPulse.first()

        val moodScore = latestCheckIn?.moodScore ?: 0.5f
        val orbColor = getOrbColor(moodScore)

        provideContent {
            DriftWidgetContent(
                latestCheckIn = latestCheckIn,
                orbColor = orbColor
            )
        }
    }
}