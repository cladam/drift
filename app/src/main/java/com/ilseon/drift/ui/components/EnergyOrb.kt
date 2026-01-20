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
import kotlin.math.roundToInt

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

@Composable
fun EnergyOrb(
    targetColor: Color,
    modifier: Modifier = Modifier,
    isMeasuring: Boolean,
    hrv: Float? = null,
    stressIndex: Float? = null,
    energy: Float = 0.5f,
) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "OrbColorTransition"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val pulseScale by if (isMeasuring) {
        // Heartbeat pulse animation driven by HRV during measurement
        val pulseDuration = hrv?.let {
            (10 * it.coerceIn(20f, 100f) + 500).roundToInt()
        } ?: 1000 // Default to 1-second pulse if HRV is not available

        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f, // A more pronounced pulse
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = pulseDuration

                    // "Lub" - The big contraction
                    1.0f at 0 using FastOutSlowInEasing
                    1.15f at (pulseDuration * 0.1).roundToInt()
                    1.05f at (pulseDuration * 0.2).roundToInt()

                    // "Dub" - The smaller subsequent contraction
                    1.10f at (pulseDuration * 0.3).roundToInt()
                    1.0f at (pulseDuration * 0.45).roundToInt()

                    // Pause (Diastole) - Stays still for the rest of the cycle
                    1.0f at pulseDuration
                },
                repeatMode = RepeatMode.Restart
            ),
            label = "HeartbeatScale"
        )
    } else {
        // Gentle breathing animation influenced by Stress Index and HRV
        // Duration from Stress: Higher stress -> faster breathing
        val breathingDuration = stressIndex?.let {
            (7000 - 200 * it.coerceIn(0f, 25f)).roundToInt()
        } ?: 3500 // Default duration

        // Amplitude from HRV: Higher HRV -> deeper breathing (larger scale)
        val breathingAmplitude = hrv?.let {
            lerp(1.05f, 1.15f, (it.coerceIn(20f, 120f) - 20f) / 100f)
        } ?: 1.08f // Default amplitude

        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = breathingAmplitude,
            animationSpec = infiniteRepeatable(
                animation = tween(breathingDuration, easing = LinearEasing),
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
        // Higher energy level results in a brighter, larger glow.
        val glowAlpha = lerp(0.2f, 0.6f, energy)
        val glowRadiusDivisor = lerp(2.2f, 1.8f, energy)

        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animatedColor.copy(alpha = glowAlpha),
                    Color.Transparent
                )
            ),
            radius = size.minDimension / glowRadiusDivisor
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
