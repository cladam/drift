package com.ilseon.drift.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.data.DriftRepository
import com.ilseon.drift.notifications.DriftNotificationManager
import com.ilseon.drift.service.SleepTrackingManager
import com.ilseon.drift.service.SleepTrackingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CheckInViewModel(
    application: Application,
    private val repository: DriftRepository
) : AndroidViewModel(application) {

    private val sleepTrackingManager = SleepTrackingManager(application)
    private val notificationManager = DriftNotificationManager(application)

    val isSleepTracking: StateFlow<Boolean> = SleepTrackingState.isTracking

    init {
        // Observe when the service auto-stops and save the sleep data
        viewModelScope.launch {
            SleepTrackingState.isTracking.collect { isTracking ->
                if (!isTracking && SleepTrackingState.sleepDurationMs.value > 0) {
                    saveSleepFromService()
                }
            }
        }
    }

    private fun saveSleepFromService() {
        viewModelScope.launch {
            val logToUpdate = latestCheckIn.first()
            val durationMs = SleepTrackingState.sleepDurationMs.value
            val endTime = SleepTrackingState.sleepEndTime.value

            if (logToUpdate != null && logToUpdate.sleepStartTime != null && durationMs > 0) {
                val sleepDurationMinutes = (durationMs / 60000).toInt()
                Log.d("CheckInViewModel", "Auto-saving sleep: $sleepDurationMinutes minutes")

                val updatedLog = logToUpdate.copy(
                    sleepEndTime = endTime,
                    sleepDurationMinutes = sleepDurationMinutes
                )
                update(updatedLog)

                // Show notification with sleep summary
                notificationManager.showSleepSummaryNotification(sleepDurationMinutes)

                // Reset the state
                SleepTrackingState.sleepDurationMs.value = 0L
                SleepTrackingState.sleepEndTime.value = 0L
            }
        }
    }

    val allLogs: StateFlow<List<DriftLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val latestCheckIn: StateFlow<DriftLog?> = repository.latestPulse
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val previousLog: StateFlow<DriftLog?> = allLogs.map {
        it.getOrNull(1) // The second most recent log
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val latestSleepRecord: StateFlow<DriftLog?> = repository.latestSleepRecord
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _showCheckInFromWidget = MutableStateFlow(false)
    val showCheckInFromWidget = _showCheckInFromWidget.asStateFlow()

    fun triggerCheckInFromWidget() {
        viewModelScope.launch {
            // This suspends until the initial value from the database is loaded into latestCheckIn
            latestCheckIn.first()
            // Now we can safely show the modal
            _showCheckInFromWidget.value = true
        }
    }

    fun onCheckInFromWidgetHandled() {
        _showCheckInFromWidget.value = false
    }

    fun insert(moodScore: Float, energyLevel: String, hrv: Double? = null, bpm: Int? = null, stressIndex: Double? = null) = viewModelScope.launch {
        val previousLog = latestCheckIn.first()

        // Create a new log that carries over previous data for unmeasured metrics.
        val newLog = DriftLog(
            moodScore = moodScore,
            energyLevel = energyLevel,
            hrvValue = hrv ?: previousLog?.hrvValue, // Use new HRV if available, else carry over.
            bpm = bpm ?: previousLog?.bpm, // Use new BPM if available, else carry over.
            stressIndex = stressIndex ?: previousLog?.stressIndex, // Use new stress index if available, else carry over.
            // Always carry over the latest sleep state.
            sleepDurationMinutes = previousLog?.sleepDurationMinutes,
            sleepStartTime = previousLog?.sleepStartTime,
            sleepEndTime = previousLog?.sleepEndTime
        )
        repository.insert(newLog)
    }

    private fun update(log: DriftLog) = viewModelScope.launch {
        repository.update(log)
    }

    fun startSleep() {
        viewModelScope.launch {
            val logToUpdate = latestCheckIn.first()
            if (logToUpdate != null) {
                val updatedLog = logToUpdate.copy(
                    sleepStartTime = System.currentTimeMillis(),
                    sleepEndTime = null
                )
                update(updatedLog)
            } else {
                val newLog = DriftLog(sleepStartTime = System.currentTimeMillis())
                repository.insert(newLog)
            }
            // Start the foreground service
            sleepTrackingManager.startSleepTracking()
        }
    }

    fun endSleep() {
        viewModelScope.launch {
            // Stop the foreground service first
            sleepTrackingManager.stopSleepTracking()

            val logToUpdate = latestCheckIn.first()
            if (logToUpdate != null && logToUpdate.sleepStartTime != null && logToUpdate.sleepEndTime == null) {
                val sleepEndTime = System.currentTimeMillis()
                val sleepStartTime = logToUpdate.sleepStartTime!!
                val sleepDuration = sleepEndTime - sleepStartTime
                val sleepDurationMinutes = (sleepDuration / (1000 * 60)).toInt()

                Log.d("CheckInViewModel", "Sleep start: $sleepStartTime")
                Log.d("CheckInViewModel", "Sleep end: $sleepEndTime")
                Log.d("CheckInViewModel", "Duration ms: $sleepDuration")
                Log.d("CheckInViewModel", "Duration min: $sleepDurationMinutes")

                val updatedLog = logToUpdate.copy(
                    sleepEndTime = sleepEndTime,
                    sleepDurationMinutes = sleepDurationMinutes
                )
                update(updatedLog)
            }
        }
    }

    val weeklyTrend: StateFlow<List<DriftLog>> = repository.getTrend(sevenDaysAgo())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val sevenDayHrvAverage: StateFlow<Double> = weeklyTrend.map { logs ->
        val morningHrvs = logs.filter { it.hrvValue != null && isMorning(it.timestamp) }.map { it.hrvValue!! }
        if (morningHrvs.isNotEmpty()) morningHrvs.average() else 60.0 // Default to a baseline
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60.0)

    val yesterdayEveningStressIndex: StateFlow<Double?> = weeklyTrend.map { logs ->
        logs.lastOrNull { it.stressIndex != null && isYesterdayEvening(it.timestamp) }?.stressIndex
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    private fun sevenDaysAgo(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return calendar.timeInMillis
    }

    private fun isMorning(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in 5..11 // 5 AM to 11 AM
    }

    private fun isYesterdayEvening(timestamp: Long): Boolean {
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)

        val logTime = Calendar.getInstance()
        logTime.timeInMillis = timestamp

        val hour = logTime.get(Calendar.HOUR_OF_DAY)

        return yesterday.get(Calendar.YEAR) == logTime.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == logTime.get(Calendar.DAY_OF_YEAR) &&
                hour in 18..23 // 6 PM to 11 PM
    }
}

class CheckInViewModelFactory(
    private val application: Application,
    private val repository: DriftRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckInViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
