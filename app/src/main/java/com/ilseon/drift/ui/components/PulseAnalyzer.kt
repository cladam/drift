package com.ilseon.drift.ui.components

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import kotlin.math.exp

class PulseAnalyzer(
    private val onPulseDetected: (Long) -> Unit
) : ImageAnalysis.Analyzer {

    private val signalBuffer = ArrayDeque<Double>(100) // Increased buffer for wavelet analysis
    private val transformedSignalBuffer = ArrayDeque<Double>(20)
    private val recentIntervals = ArrayDeque<Long>(10)

    private var lastPeakTime = 0L
    private val minPeakInterval = 550L  // ~109 BPM max
    private val maxPeakInterval = 1200L // ~50 BPM min
    private var frameCount = 0
    private var baselineEstablished = false

    private val waveletKernel: DoubleArray

    init {
        // Generate a Mexican Hat wavelet kernel.
        // The size should correspond to the expected width of a heartbeat pulse in samples.
        // Assuming ~30fps, a pulse is ~5-8 samples wide. We use a slightly larger kernel.
        waveletKernel = generateMexicanHatWavelet(12, 2.0)
    }

    /**
     * Generates a kernel for a Mexican Hat wavelet (or Ricker wavelet),
     * which is the second derivative of a Gaussian function.
     * This is effective for detecting peaks in a signal.
     */
    private fun generateMexicanHatWavelet(size: Int, sigma: Double): DoubleArray {
        val kernel = DoubleArray(size)
        val halfSize = (size - 1) / 2.0
        val sigma2 = sigma * sigma
        var sum = 0.0
        for (i in 0 until size) {
            val x = i - halfSize
            val x2 = x * x
            // Formula for the second derivative of a Gaussian
            val term1 = (x2 - sigma2) / (sigma2 * sigma2)
            val term2 = exp(-x2 / (2 * sigma2))
            kernel[i] = term1 * term2
            sum += kernel[i]
        }
        // Normalize to have a zero mean, which is a key property of a wavelet
        val mean = sum / size
        for (i in kernel.indices) {
            kernel[i] -= mean
        }
        return kernel
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        if (image.format != android.graphics.ImageFormat.YUV_420_888) {
            image.close()
            return
        }

        // The image analysis part to get the average red value remains the same.
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
                // Simplified red channel approximation using just the V plane
                // R ~ V. This is less accurate than Y+V but much faster.
                // Since we care about change, not absolute value, this works.
                val vIndex = (y / 2) * vPlane.rowStride + (x / 2) * vPlane.pixelStride
                val vValue = vBuffer.get(vIndex).toInt() and 0xFF
                sumRed += vValue
                count++
            }
        }
        image.close()

        val currentValue = if (count > 0) sumRed / count else 0.0
        val currentTime = System.currentTimeMillis()
        frameCount++

        signalBuffer.addLast(currentValue)
        if (signalBuffer.size > 100) signalBuffer.removeFirst()

        // Wait for the signal buffer to fill before starting analysis
        if (signalBuffer.size < waveletKernel.size) {
            return
        }

        // Establish a baseline for logging
        if (!baselineEstablished && frameCount > 20) {
            baselineEstablished = true
            android.util.Log.d("PulseAnalyzer", "Baseline established. Initial Red Avg: ${String.format("%.1f", currentValue)}")
        }
        if (!baselineEstablished) {
            return
        }

        // --- Wavelet Transform ---
        // Convolve the latest part of the signal with the wavelet kernel
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

        // --- Peak Detection on Transformed Signal ---
        // A peak is the local maximum in the transformed signal history.
        val isPeak = transformedSignalBuffer[transformedSignalBuffer.size - 2] > transformedSignalBuffer[transformedSignalBuffer.size - 3] &&
                     transformedSignalBuffer[transformedSignalBuffer.size - 2] > transformedSignalBuffer[transformedSignalBuffer.size - 1] &&
                     transformedSignalBuffer[transformedSignalBuffer.size - 2] > 0 // Ensure it's a positive peak

        val timeSinceLastPeak = currentTime - lastPeakTime

        if (isPeak && timeSinceLastPeak >= minPeakInterval) {
            val isValidInterval = timeSinceLastPeak <= maxPeakInterval || lastPeakTime == 0L

            // Check if the new beat interval is consistent with recent history.
            // This is a crucial step to filter out outliers.
            val isConsistent = if (recentIntervals.size >= 3 && lastPeakTime != 0L) {
                val avgInterval = recentIntervals.average()
                val deviation = kotlin.math.abs(timeSinceLastPeak - avgInterval) / avgInterval
                deviation < 0.30 // Only accept beats within 30% of the recent average interval
            } else {
                true // Not enough history to check consistency, so accept it.
            }

            if (isValidInterval && isConsistent) {
                onPulseDetected(currentTime)
                if (lastPeakTime != 0L) {
                    recentIntervals.addLast(timeSinceLastPeak)
                    if (recentIntervals.size > 10) recentIntervals.removeFirst()
                    android.util.Log.d("PulseAnalyzer", "Pulse: ${timeSinceLastPeak}ms (Wavelet)")
                } else {
                    android.util.Log.d("PulseAnalyzer", "First pulse detected (Wavelet)")
                }
                lastPeakTime = currentTime
            }
        }
    }
}
