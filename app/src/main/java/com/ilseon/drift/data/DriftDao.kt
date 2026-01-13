package com.ilseon.drift.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ilseon.drift.data.DriftLog
import kotlinx.coroutines.flow.Flow


@Dao
interface DriftDao {
    @Insert
    suspend fun insertLog(log: DriftLog)

    @Query("SELECT * FROM drift_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestPulse(): Flow<DriftLog?>

    @Query("SELECT * FROM drift_logs WHERE timestamp > :since ORDER BY timestamp ASC")
    fun getTrendLogs(since: Long): Flow<List<DriftLog>>
}