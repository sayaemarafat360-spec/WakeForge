package com.wakeforge.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.wakeforge.app.data.database.entity.AlarmEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {

    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    @Query("SELECT * FROM alarms WHERE id = :id")
    fun getAlarmById(id: String): Flow<AlarmEntity?>

    @Query("SELECT * FROM alarms WHERE isActive = 1 ORDER BY hour, minute LIMIT 1")
    fun getFirstActiveAlarm(): Flow<AlarmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlarm(alarm: AlarmEntity)

    @Update
    suspend fun updateAlarm(alarm: AlarmEntity)

    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteAlarm(id: String)

    @Query("UPDATE alarms SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveState(id: String, isActive: Boolean)

    @Query("SELECT * FROM alarms WHERE isActive = 1 ORDER BY hour, minute")
    suspend fun getActiveAlarmsOnce(): List<AlarmEntity>

    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getAlarmByIdOnce(id: String): AlarmEntity?
}
