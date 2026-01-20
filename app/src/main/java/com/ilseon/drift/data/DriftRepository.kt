package com.ilseon.drift.data

import com.ilseon.drift.data.DriftDao
import com.ilseon.drift.data.DriftLog
import kotlinx.coroutines.flow.Flow

class DriftRepository(private val driftDao: DriftDao) {

    val allLogs: Flow<List<DriftLog>> = driftDao.getAll()
    val latestPulse: Flow<DriftLog?> = driftDao.getLatestPulse()
    val latestSleepRecord: Flow<DriftLog?> = driftDao.getLatestSleepRecord()

    suspend fun insert(log: DriftLog) {
        driftDao.insertLog(log)
    }

    suspend fun update(log: DriftLog) {
        driftDao.updateLog(log)
    }

    fun getTrend(since: Long): Flow<List<DriftLog>> {
        return driftDao.getTrendLogs(since)
    }
}