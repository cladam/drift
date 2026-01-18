package com.ilseon.drift.ui.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.collections.removeFirst
import kotlin.compareTo
import kotlin.div
import kotlin.text.compareTo

class PulseAnalyzer(
    private val onPulseDetected: (Long) -> Unit
) : ImageAnalysis.Analyzer {

    private val signalBuffer = ArrayDeque<Double>(60)
    private val recentIntervals = ArrayDeque<Long>(5)
    private var lastPeakTime = 0L
    private val minPeakInterval = 550L  // ~109 BPM max
    private val maxPeakInterval = 1200L // ~50 BPM min
    private var frameCount = 0
    private var baselineEstablished = false

    private var prevSmoothed = 0.0
    private var prevPrevSmoothed = 0.0

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = image.width
        val height = image.height
        var sum = 0L
        var count = 0

        val startX = width / 3
        val endX = 2 * width / 3
        val startY = height / 3
        val endY = 2 * height / 3

        for (y in startY until endY step 2) {
            for (x in startX until endX step 2) {
                val index = y * width + x
                if (index < data.size) {
                    sum += data[index].toInt() and 0xFF
                    count++
                }
            }
        }

        val currentValue = if (count > 0) sum.toDouble() / count else 0.0
        val currentTime = System.currentTimeMillis()
        frameCount++

        if (currentValue !in 40.0..250.0) {
            image.close()
            return
        }

        signalBuffer.addLast(currentValue)
        if (signalBuffer.size > 60) signalBuffer.removeFirst()

        if (signalBuffer.size < 10) {
            image.close()
            return
        }

        val smoothed = signalBuffer.takeLast(3).average()

        if (!baselineEstablished && frameCount > 20) {
            baselineEstablished = true
            android.util.Log.d("PulseAnalyzer", "Ready, avg: ${"%.1f".format(smoothed)}")
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
        val isSignificant = dynamicRange > 1.5 && peakHeight > dynamicRange * 0.4

        if (frameCount % 15 == 0) {
            android.util.Log.d("PulseAnalyzer", "val=${"%.1f".format(smoothed)}, range=${"%.1f".format(dynamicRange)}, peak=$isPeak")
        }

        if (isPeak && isSignificant && timeSinceLastPeak >= minPeakInterval) {
            val isValidInterval = timeSinceLastPeak <= maxPeakInterval || lastPeakTime == 0L

            // Consistency check: skip for first few pulses while building history
            val isConsistent = if (recentIntervals.size >= 3 && lastPeakTime != 0L) {
                val avgInterval = recentIntervals.average()
                val deviation = kotlin.math.abs(timeSinceLastPeak - avgInterval) / avgInterval
                deviation < 0.35  // deviation
            } else true

            if (isValidInterval && isConsistent) {
                onPulseDetected(currentTime)
                if (lastPeakTime != 0L) {
                    recentIntervals.addLast(timeSinceLastPeak)
                    if (recentIntervals.size > 5) recentIntervals.removeFirst()
                    android.util.Log.d("PulseAnalyzer", "Pulse: ${timeSinceLastPeak}ms")
                } else {
                    android.util.Log.d("PulseAnalyzer", "First pulse detected")
                }
            }
            lastPeakTime = currentTime
        }

        prevPrevSmoothed = prevSmoothed
        prevSmoothed = smoothed
        image.close()
    }
}
