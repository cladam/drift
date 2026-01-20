package com.ilseon.drift.service

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A singleton object to hold the shared state of sleep tracking.
 * This is a simple and modern alternative to using broadcasts for internal app communication.
 */
object SleepTrackingState {
    val isTracking = MutableStateFlow(false)
    val sleepEndTime = MutableStateFlow(0L)
    val sleepDurationMs = MutableStateFlow(0L)
}
