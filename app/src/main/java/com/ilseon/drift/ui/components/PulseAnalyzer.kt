package com.ilseon.drift.ui.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.exp

class PulseAnalyzer(
    private val onPulseDetected: (Long) -> Unit,
    private val onMeasurementFailed: () -> Unit
) : ImageAnalysis.Analyzer {

    private val signalBuffer = ArrayDeque<Double>(100) // Increased buffer for wavelet analysis
    private val transformedSignalBuffer = ArrayDeque<Double>(20)
    private val recentIntervals = ArrayDeque<Long>(10)

    private var lastPeakTime = 0L
    private val minPeakInterval = 550L  // ~109 BPM max
    private val maxPeakInterval = 1200L // ~50 BPM min
    private var frameCount = 0
    private var baselineEstablished = false
    private var startTime = 0L
    private var lastPulseTime = 0L
    private val measurementTimeout = 5000L // 5 seconds

    private val waveletKernel: DoubleArray

    init {
        // Generate a Mexican Hat wavelet kernel.
        waveletKernel = generateMexicanHatWavelet(12, 2.0)
    }

    private fun generateMexicanHatWavelet(size: Int, sigma: Double): DoubleArray {
        val kernel = DoubleArray(size)
        val halfSize = (size - 1) / 2.0
        val sigma2 = sigma * sigma
        var sum = 0.0
        for (i in 0 until size) {
            val x = i - halfSize
            val x2 = x * x
            val term1 = (x2 - sigma2) / (sigma2 * sigma2)
            val term2 = exp(-x2 / (2 * sigma2))
            kernel[i] = term1 * term2
            sum += kernel[i]
        }
        val mean = sum / size
        for (i in kernel.indices) {
            kernel[i] -= mean
        }
        return kernel
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        if (startTime == 0L) {
            startTime = currentTime
            lastPulseTime = currentTime
        }

        if (frameCount > 60 && currentTime - lastPulseTime > measurementTimeout) {
            onMeasurementFailed()
            lastPulseTime = currentTime // Reset to avoid repeated calls
        }

        if (image.format != android.graphics.ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        val vPlane = image.planes[2]
        val vBuffer = vPlane.buffer
        val width = image.width
        val height = image.height

        var sumRed = 0.0
        var count = 0
        val startX = width / 3
        val endX = 2 * width / 3
        val startY = height / 3
        val endY = 2 * height / 3

        for (y in startY until endY step 2) {
            for (x in startX until endX step 2) {
                val vIndex = (y / 2) * vPlane.rowStride + (x / 2) * vPlane.pixelStride
                val vValue = vBuffer.get(vIndex).toInt() and 0xFF
                sumRed += vValue
                count++
            }
        }
        image.close()

        val currentValue = if (count > 0) sumRed / count else 0.0
        frameCount++

        signalBuffer.addLast(currentValue)
        if (signalBuffer.size > 100) signalBuffer.removeFirst()

        if (signalBuffer.size < waveletKernel.size) {
            return
        }

        if (!baselineEstablished && frameCount > 20) {
            baselineEstablished = true
        }
        if (!baselineEstablished) {
            return
        }

        val signalWindow = signalBuffer.takeLast(waveletKernel.size)
        var transformedValue = 0.0
        for (i in signalWindow.indices) {
            transformedValue += signalWindow[i] * waveletKernel[i]
        }
        transformedSignalBuffer.addLast(transformedValue)
        if (transformedSignalBuffer.size > 20) transformedSignalBuffer.removeFirst()

        if (transformedSignalBuffer.size < 3) {
            return
        }

        val isPeak = transformedSignalBuffer[transformedSignalBuffer.size - 2] > transformedSignalBuffer[transformedSignalBuffer.size - 3] &&
                transformedSignalBuffer[transformedSignalBuffer.size - 2] > transformedSignalBuffer[transformedSignalBuffer.size - 1] &&
                transformedSignalBuffer[transformedSignalBuffer.size - 2] > 0

        val timeSinceLastPeak = currentTime - lastPeakTime

        if (isPeak && timeSinceLastPeak >= minPeakInterval) {
            val isValidInterval = timeSinceLastPeak <= maxPeakInterval || lastPeakTime == 0L

            val isConsistent = if (recentIntervals.size >= 3 && lastPeakTime != 0L) {
                val avgInterval = recentIntervals.average()
                val deviation = kotlin.math.abs(timeSinceLastPeak - avgInterval) / avgInterval
                val maxDeviation = if (System.currentTimeMillis() - startTime < 10000) 0.50 else 0.35
                deviation < maxDeviation
            } else {
                true
            }

            if (isValidInterval && isConsistent) {
                onPulseDetected(currentTime)
                lastPulseTime = currentTime
                if (lastPeakTime != 0L) {
                    recentIntervals.addLast(timeSinceLastPeak)
                    if (recentIntervals.size > 10) recentIntervals.removeFirst()
                }
                lastPeakTime = currentTime
            }
        }
    }
}