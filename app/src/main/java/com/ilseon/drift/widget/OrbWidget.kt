package com.ilseon.drift.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import com.ilseon.drift.MainActivity
import com.ilseon.drift.R
import com.ilseon.drift.data.DriftDatabase
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent
import kotlinx.coroutines.flow.first

class OrbWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = DriftRepository(DriftDatabase.getDatabase(context).driftDao())
        val latestCheckIn = repository.latestPulse.first()

        val moodScore = latestCheckIn?.moodScore ?: 0.5f
        val orbColor = getOrbColor(moodScore)

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(
                        actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                putExtra("notification_action", "check_in")
                            }
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_launcher),
                    contentDescription = "Energy Orb",
                    modifier = GlanceModifier.size(80.dp),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(
                            day = orbColor,
                            night = orbColor
                        )
                    )
                )
            }
        }
    }

    private fun getOrbColor(moodScore: Float): Color {
        return if (moodScore > 0.5f) {
            val fraction = (moodScore - 0.5f) * 2
            lerpColor(StatusMedium, StatusHigh, fraction)
        } else {
            val fraction = moodScore * 2
            lerpColor(StatusUrgent, StatusMedium, fraction)
        }
    }

    private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
        return Color(
            red = start.red + (stop.red - start.red) * fraction,
            green = start.green + (stop.green - start.green) * fraction,
            blue = start.blue + (stop.blue - start.blue) * fraction,
            alpha = start.alpha + (stop.alpha - start.alpha) * fraction
        )
    }
}