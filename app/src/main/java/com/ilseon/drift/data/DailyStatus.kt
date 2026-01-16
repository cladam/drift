package com.ilseon.drift.data

data class DailyStatus(
    val timestamp: Long,
    val status: Status
)

enum class Status {
    LOW,
    MEDIUM,
    HIGH
}
