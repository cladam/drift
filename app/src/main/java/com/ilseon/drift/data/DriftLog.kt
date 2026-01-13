package com.ilseon.drift.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drift_logs")
data class DriftLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),

    // Subjective Data (The "Active" Slider)
    val moodScore: Float, // 0.0 (Tense) to 1.0 (Calm)
    val energyLevel: String, // "LOW", "MEDIUM", "HIGH"

    // Objective Data (The "Passive" Health Connect data)
    val sleepDurationMinutes: Int? = null,
    val hrvValue: Double? = null, // Heart Rate Variability
    val stressScore: Int? = null    // 1-100 scale from sensors
)