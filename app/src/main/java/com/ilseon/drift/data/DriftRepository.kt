package com.ilseon.drift.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class DriftRepository(private val driftDao: DriftDao) {

    val latestPulse: Flow<DriftLog?> = driftDao.getLatestPulse()

    fun getTrendLogs(since: Long): Flow<List<DriftLog>> {
        return driftDao.getTrendLogs(since)
    }

    @WorkerThread
    suspend fun insert(driftLog: DriftLog) {
        driftDao.insertLog(driftLog)
    }
}
