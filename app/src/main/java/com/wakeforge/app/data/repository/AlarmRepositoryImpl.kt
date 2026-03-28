package com.wakeforge.app.data.repository

import com.wakeforge.app.data.alarm.AlarmScheduler
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.data.database.entity.AlarmEntity
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val alarmDao: AlarmDao,
    private val alarmScheduler: AlarmScheduler
) : AlarmRepository {

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return alarmDao.getAllAlarms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getAlarmById(id: String): Flow<Alarm?> {
        return alarmDao.getAlarmById(id).map { entity ->
            entity?.toDomain()
        }
    }

    override fun getNextActiveAlarm(): Flow<Alarm?> {
        return alarmDao.getFirstActiveAlarm().map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun createAlarm(alarm: Alarm) {
        val entity = AlarmEntity.fromDomain(alarm)
        alarmDao.insertAlarm(entity)
        alarmScheduler.scheduleAlarm(alarm)
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        val updatedAlarm = alarm.copy(updatedAt = System.currentTimeMillis())
        alarmScheduler.cancelAlarm(updatedAlarm.id)
        val entity = AlarmEntity.fromDomain(updatedAlarm)
        alarmDao.updateAlarm(entity)
        alarmScheduler.scheduleAlarm(updatedAlarm)
    }

    override suspend fun deleteAlarm(id: String) {
        alarmScheduler.cancelAlarm(id)
        alarmDao.deleteAlarm(id)
    }

    override suspend fun toggleAlarm(id: String, isActive: Boolean) {
        alarmDao.updateActiveState(id, isActive)
        val entity = alarmDao.getAlarmByIdOnce(id)
        if (entity != null) {
            if (isActive) {
                alarmScheduler.scheduleAlarm(entity.toDomain())
            } else {
                alarmScheduler.cancelAlarm(id)
            }
        }
    }

    override suspend fun scheduleAlarm(alarm: Alarm) {
        alarmScheduler.scheduleAlarm(alarm)
    }

    override suspend fun cancelAlarm(alarmId: String) {
        alarmScheduler.cancelAlarm(alarmId)
    }

    override suspend fun cancelAllAlarms() {
        val activeAlarms = alarmDao.getActiveAlarmsOnce()
        for (entity in activeAlarms) {
            alarmScheduler.cancelAlarm(entity.id)
        }
    }
}
