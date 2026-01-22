package com.ilseon.drift.processing

import android.util.Log
import kotlin.compareTo
import kotlin.div
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.text.toDouble
import kotlin.times

// Artifact correction based on HRV4Training's methodology.
private fun correctArtifacts(intervals: List<Long>): List<Long> {
    if (intervals.size < 6) return intervals // Not enough data to apply the filter
    val cleaned = mutableListOf<Long>()
    cleaned.add(intervals.first()) // Assume first beat is okay

    for (i in 1 until intervals.size) {
        val recentAverage = cleaned.takeLast(5).average()
        // Avoid division by zero if average is 0
        if (recentAverage == 0.0) {
            cleaned.add(intervals[i])
            continue
        }

        val currentInterval = intervals[i]

        // Keep interval if it's within 20% of the rolling average
        if (abs(currentInterval - recentAverage) / recentAverage <= 0.20) {
            cleaned.add(currentInterval)
        }
    }
    Log.d("ArtifactCorrection", "Original: ${intervals.size}, Cleaned: ${cleaned.size}")
    return cleaned
}

fun calculateRmssdFromIntervals(intervals: List<Long>): Double? {
    if (intervals.size < 2) return null
    val cleanedIntervals = correctArtifacts(intervals)
    if (cleanedIntervals.size < 2) return null

    val squaredDiffs = cleanedIntervals.zipWithNext { a, b -> ((b - a) * (b - a)).toDouble() }
    val meanSquaredDiff = squaredDiffs.average()

    return if (meanSquaredDiff.isNaN()) null else sqrt(meanSquaredDiff)
}

fun calculateBpmFromIntervals(intervals: List<Long>): Int {
    if (intervals.isEmpty()) return 0
    val cleanedIntervals = correctArtifacts(intervals)
    if (cleanedIntervals.isEmpty()) return 0
    val avgInterval = cleanedIntervals.average()
    return if (avgInterval > 0) (60000.0 / avgInterval).toInt() else 0
}

fun calculateStressIndex(rrIntervals: List<Long>): Double? {
    val cleanedIntervals = correctArtifacts(rrIntervals)
    if (cleanedIntervals.size < 2) return null

    val maxRR = cleanedIntervals.maxOrNull()?.toDouble() ?: return null
    val minRR = cleanedIntervals.minOrNull()?.toDouble() ?: return null
    val mxDMn = (maxRR - minRR) / 1000.0 // seconds
    if (mxDMn == 0.0) return null

    val bins = cleanedIntervals.map { (it / 50) * 50 }
    val modeBin = bins.groupBy { it }.maxByOrNull { it.value.size }?.key ?: return null
    val mo = modeBin / 1000.0 // seconds
    if (mo == 0.0) return null

    val modeCount = bins.count { it == modeBin }
    val aMo = (modeCount.toDouble() / cleanedIntervals.size) * 100.0

    val baevskySI = aMo / (2.0 * mo * mxDMn)

    val kubiosSI = sqrt(baevskySI)

    return if (kubiosSI.isFinite()) kubiosSI else null
}
