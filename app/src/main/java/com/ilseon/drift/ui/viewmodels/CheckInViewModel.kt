package com.ilseon.drift.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ilseon.drift.data.DriftLog
import com.ilseon.drift.data.DriftRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class CheckInViewModel(private val repository: DriftRepository) : ViewModel() {

    val latestCheckIn: StateFlow<DriftLog?> = repository.latestPulse
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun insert(moodScore: Float, energyLevel: String) = viewModelScope.launch {
        val newLog = DriftLog(
            moodScore = moodScore,
            energyLevel = energyLevel
        )
        repository.insert(newLog)
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
