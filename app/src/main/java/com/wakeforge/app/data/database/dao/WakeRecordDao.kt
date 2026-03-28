package com.wakeforge.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wakeforge.app.data.database.entity.WakeRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WakeRecordDao {

    @Insert
    suspend fun insertRecord(record: WakeRecordEntity)

    @Query("SELECT * FROM wake_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<WakeRecordEntity>>

    @Query("SELECT * FROM wake_records WHERE timestamp >= :startDate ORDER BY timestamp DESC")
    fun getRecordsSince(startDate: Long): Flow<List<WakeRecordEntity>>

    @Query("SELECT COUNT(*) FROM wake_records WHERE outcome = 'SUCCESS' AND timestamp >= :startDate")
    suspend fun getSuccessCountSince(startDate: Long): Int

    @Query("SELECT COUNT(*) FROM wake_records WHERE timestamp >= :startDate")
    suspend fun getTotalCountSince(startDate: Long): Int

    @Query("SELECT COUNT(*) FROM wake_records WHERE outcome = 'SNOOZE' AND timestamp >= :startDate")
    suspend fun getSnoozeCountSince(startDate: Long): Int

    @Query("SELECT COUNT(*) FROM wake_records WHERE outcome = 'FAILURE' AND timestamp >= :startDate")
    suspend fun getFailureCountSince(startDate: Long): Int

    @Query("SELECT AVG(completionTimeMs) FROM wake_records WHERE outcome = 'SUCCESS' AND timestamp >= :startDate")
    suspend fun getAverageCompletionTimeSince(startDate: Long): Double?

    @Query("SELECT * FROM wake_records WHERE alarmId = :alarmId ORDER BY timestamp DESC")
    fun getRecordsForAlarm(alarmId: String): Flow<List<WakeRecordEntity>>
}
