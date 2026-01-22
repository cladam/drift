package com.ilseon.drift.ui.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

class PulseAnalyzer(
    private val onPulseDetected: (Long) -> Unit
) : ImageAnalysis.Analyzer {

    private val signalBuffer = ArrayDeque<Double>(60)
    private val recentIntervals = ArrayDeque<Long>(10)
    private var lastPeakTime = 0L
    private val minPeakInterval = 550L  // ~109 BPM max
    private val maxPeakInterval = 1200L // ~50 BPM min
    private var frameCount = 0
    private var baselineEstablished = false

    private var prevSmoothed = 0.0
    private var prevPrevSmoothed = 0.0

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (image.format != android.graphics.ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val yPlane = image.planes[0]
        val vPlane = image.planes[2] // V plane for red-difference chroma

        val yBuffer = yPlane.buffer
        val vBuffer = vPlane.buffer

        val width = image.width
        val height = image.height

        var sumRed = 0.0
        var count = 0

        // Focus on the center of the frame
        val startX = width / 3
        val endX = 2 * width / 3
        val startY = height / 3
        val endY = 2 * height / 3

        // We only need Y and V for the red channel. Using step 2 for performance.
        for (y in startY until endY step 2) {
            for (x in startX until endX step 2) {
                val yIndex = y * yPlane.rowStride + x * yPlane.pixelStride
                val yValue = yBuffer.get(yIndex).toInt() and 0xFF

                // V plane is subsampled by 2.
                val vIndex = (y / 2) * vPlane.rowStride + (x / 2) * vPlane.pixelStride
                val vValue = vBuffer.get(vIndex).toInt() and 0xFF

                // YUV to Red conversion: R = Y + 1.402 * (V - 128)
                val red = yValue + 1.402 * (vValue - 128)

                sumRed += red
                count++
            }
        }

        val currentValue = if (count > 0) sumRed / count else 0.0
        val currentTime = System.currentTimeMillis()
        frameCount++

        // The rest of the analysis logic remains the same, operating on the new `currentValue`.

        signalBuffer.addLast(currentValue)
        if (signalBuffer.size > 60) signalBuffer.removeFirst()

        if (signalBuffer.size < 10) {
            image.close()
            return
        }

        val smoothed = signalBuffer.takeLast(3).average()

        if (!baselineEstablished && frameCount > 20) {
            baselineEstablished = true
            android.util.Log.d("PulseAnalyzer", "Baseline established. Initial Red Avg: ${String.format("%.1f", smoothed)}")
        }

        if (!baselineEstablished) {
            image.close()
            return
        }

        val recentWindow = signalBuffer.takeLast(20)
        val windowMax = recentWindow.maxOrNull() ?: smoothed
        val windowMin = recentWindow.minOrNull() ?: smoothed
        val dynamicRange = windowMax - windowMin

        val timeSinceLastPeak = currentTime - lastPeakTime

        // Local peak detection
        val isPeak = prevSmoothed > prevPrevSmoothed && prevSmoothed > smoothed

        val peakHeight = prevSmoothed - windowMin
        // Dynamic range check helps filter out noise when the finger isn't covering the camera well.
        val isSignificant = dynamicRange > 0.5 && peakHeight > dynamicRange * 0.4

        if (isPeak && isSignificant && timeSinceLastPeak >= minPeakInterval) {
            val isValidInterval = timeSinceLastPeak <= maxPeakInterval || lastPeakTime == 0L

            val isConsistent = if (recentIntervals.size >= 3 && lastPeakTime != 0L) {
                val avgInterval = recentIntervals.average()
                val deviation = kotlin.math.abs(timeSinceLastPeak - avgInterval) / avgInterval
                deviation < 0.35 // Only accept beats that are within 35% of the recent average interval
            } else true

            if (isValidInterval && isConsistent) {
                onPulseDetected(currentTime)
                if (lastPeakTime != 0L) {
                    recentIntervals.addLast(timeSinceLastPeak)
                    if (recentIntervals.size > 10) recentIntervals.removeFirst()
                    android.util.Log.d("PulseAnalyzer", "Pulse: ${timeSinceLastPeak}ms (Red Channel)")
                } else {
                    android.util.Log.d("PulseAnalyzer", "First pulse detected (Red Channel)")
                }
            }
            lastPeakTime = currentTime
        }

        prevPrevSmoothed = prevSmoothed
        prevSmoothed = smoothed
        image.close()
    }
}
