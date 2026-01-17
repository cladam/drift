package com.ilseon.drift.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun EnergyOrb(
    targetColor: Color,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = false,
) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "OrbColorTransition"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val pulseScale by if (isPulsing) {
        // Heartbeat pulse animation
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f, // A more pronounced pulse
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000 // Motsvarar ca 60 BPM

                    // "Lub" - Den stora sammandragningen
                    1.0f at 0 using FastOutSlowInEasing
                    1.15f at 100
                    1.05f at 200

                    // "Dub" - Den mindre efterföljande sammandragningen
                    1.10f at 300
                    1.0f at 450

                    // Paus (Diastole) - Håller sig stilla resten av sekunden
                    1.0f at 1000
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "HeartbeatScale"
        )
    } else {
        // Original gentle breathing animation
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(3500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "BreathingScale"
        )
    }

    Canvas(
        modifier = modifier
            .size(240.dp)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
    ) {
        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animatedColor.copy(alpha = 0.4f),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / 2
        )

        // Main orb with a gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animatedColor.copy(alpha = 0.9f),
                    animatedColor.copy(alpha = 0.2f)
                )
            ),
            radius = size.minDimension / 2.5f
        )
    }
}