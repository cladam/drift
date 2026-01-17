package com.ilseon.drift.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
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
) {
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 1000),
        label = "OrbColorTransition"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

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