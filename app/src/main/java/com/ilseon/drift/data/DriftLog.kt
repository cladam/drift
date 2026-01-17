package com.ilseon.drift.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drift_logs")
data class DriftLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),

    // Subjective Data (The "Active" Slider)
    var moodScore: Float? = null, // 0.0 (Tense) to 1.0 (Calm)
    var energyLevel: String? = null, // "LOW", "MEDIUM", "HIGH"

    // Objective Data (The "Passive" Health Connect data)
    var sleepDurationMinutes: Int? = null,
    var sleepStartTime: Long? = null,
    var sleepEndTime: Long? = null,
    var hrvValue: Double? = null, // Heart Rate Variability
    var bpm: Int? = null, // Beats Per Minute
    var stressScore: Int? = null,    // 1-100 scale from sensors
    var stressIndex: Double? = null
)
