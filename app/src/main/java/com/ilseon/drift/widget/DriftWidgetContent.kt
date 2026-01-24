package com.ilseon.drift.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.ilseon.drift.MainActivity
import com.ilseon.drift.R
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.ui.theme.CustomTextPrimary
import com.ilseon.drift.ui.theme.CustomTextSecondary
import com.ilseon.drift.ui.theme.LightGrey
import com.ilseon.drift.ui.theme.StatusHigh
import com.ilseon.drift.ui.theme.StatusMedium
import com.ilseon.drift.ui.theme.StatusUrgent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DriftWidgetContent(
    latestCheckIn: DriftLog?,
    orbColor: Color,
    isSleepTracking: Boolean
) {
    val formattedTime = latestCheckIn?.timestamp?.let {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(it))
    } ?: "N/A"

    GlanceTheme {
        Row(
            modifier = GlanceModifier
                .background(LightGrey)
                .appWidgetBackground()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Mini Energy Orb
            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .clickable(actionStartActivity(Intent(Intent.ACTION_VIEW).setClassName(
                        "com.ilseon.drift",
                        "com.ilseon.drift.MainActivity"
                    )))
            ) {
                Image(
                    provider = ImageProvider(R.drawable.orb),
                    contentDescription = "Energy Orb",
                    modifier = GlanceModifier.fillMaxSize()
                )
            }
            Spacer(GlanceModifier.width(16.dp))
            if (isSleepTracking) {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        "Tracking Sleep...",
                        style = TextStyle(color = ColorProvider(day = CustomTextPrimary, night = CustomTextPrimary), fontSize = 14.sp)
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    WidgetButton(
                        text = "Wake Up",
                        onClick = actionRunCallback<EndSleepAction>()
                    )
                }
            } else {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    WidgetButton(
                        text = "Goodnight",
                        onClick = actionRunCallback<StartSleepAction>()
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    WidgetButton(
                        text = "Check In",
                        onClick = actionStartActivity(Intent(Intent.ACTION_VIEW).setClassName(
                            "com.ilseon.drift",
                            "com.ilseon.drift.MainActivity"
                        ).apply {
                            putExtra("notification_action", "check_in")
                        })
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        "Last Log: $formattedTime",
                        style = TextStyle(color = ColorProvider(day = CustomTextSecondary, night = CustomTextSecondary), fontSize = 12.sp)
                    )
                }
            }

        }
    }
}

@Composable
private fun WidgetButton(text: String, onClick: Action) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .clickable(onClick)
            .background(Color(0xFF27262A))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text, style = TextStyle(color = ColorProvider(day = CustomTextPrimary, night = CustomTextPrimary)))
    }
}

fun getOrbColor(moodScore: Float): Color {
    return if (moodScore > 0.5f) {
        val fraction = (moodScore - 0.5f) * 2
        lerpColor(StatusMedium, StatusHigh, fraction)
    } else {
        val fraction = moodScore * 2
        lerpColor(StatusUrgent, StatusMedium, fraction)
    }
}

fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}
