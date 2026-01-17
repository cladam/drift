package com.ilseon.drift.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.data.DriftRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.div
import kotlin.text.toInt

class CheckInViewModel(private val repository: DriftRepository) : ViewModel() {

    val latestCheckIn: StateFlow<DriftLog?> = repository.latestPulse
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val latestSleepRecord: StateFlow<DriftLog?> = repository.latestSleepRecord
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    fun insert(moodScore: Float, energyLevel: String, hrv: Double? = null, bpm: Int? = null) = viewModelScope.launch {
        val previousLog = latestCheckIn.first()

        // Create a new log that carries over previous data for unmeasured metrics.
        val newLog = DriftLog(
            moodScore = moodScore,
            energyLevel = energyLevel,
            hrvValue = hrv ?: previousLog?.hrvValue, // Use new HRV if available, else carry over.
            bpm = bpm ?: previousLog?.bpm, // Use new BPM if available, else carry over.
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
                // Update the most recent log with the sleep start time
                val updatedLog = logToUpdate.copy(
                    sleepStartTime = System.currentTimeMillis(),
                    sleepEndTime = null // Ensure end time is cleared for the isSleeping state
                )
                update(updatedLog)
            } else {
                // If no logs exist, create a new one to start tracking sleep
                val newLog = DriftLog(sleepStartTime = System.currentTimeMillis())
                repository.insert(newLog)
            }
        }
    }

    fun endSleep() {
        viewModelScope.launch {
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

    private fun sevenDaysAgo(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        return calendar.timeInMillis
    }
}

class CheckInViewModelFactory(private val repository: DriftRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckInViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CheckInViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
