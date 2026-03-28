package com.wakeforge.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey
    val id: String,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: String,
    val soundUri: String,
    val vibrationEnabled: Boolean,
    val gradualVolumeEnabled: Boolean,
    val gradualVolumeDurationSeconds: Int,
    val missionType: String,
    val difficulty: String,
    val snoozeIntervalMinutes: Int,
    val maxSnoozeCount: Int,
    val smartEscalationEnabled: Boolean,
    val strictModeEnabled: Boolean,
    val multiStepEnabled: Boolean,
    val multiStepCount: Int,
    val timedModeEnabled: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {

    fun toDomain(): Alarm {
        val repeatDayList = repeatDays
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { dayStr ->
                dayStr.toIntOrNull()?.let { dayIndex ->
                    DayOfWeek.entries.firstOrNull { it.calendarDay == dayIndex }
                }
            }

        return Alarm(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            repeatDays = repeatDayList,
            soundUri = soundUri,
            vibrationEnabled = vibrationEnabled,
            gradualVolumeEnabled = gradualVolumeEnabled,
            gradualVolumeDurationSeconds = gradualVolumeDurationSeconds,
            missionType = try {
                MissionType.valueOf(missionType)
            } catch (e: IllegalArgumentException) {
                MissionType.MATH
            },
            difficulty = try {
                MissionDifficulty.valueOf(difficulty)
            } catch (e: IllegalArgumentException) {
                MissionDifficulty.MEDIUM
            },
            snoozeIntervalMinutes = snoozeIntervalMinutes,
            maxSnoozeCount = maxSnoozeCount,
            smartEscalationEnabled = smartEscalationEnabled,
            strictModeEnabled = strictModeEnabled,
            multiStepEnabled = multiStepEnabled,
            multiStepCount = multiStepCount,
            timedModeEnabled = timedModeEnabled,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity {
            val repeatDaysStr = alarm.repeatDays
                .sortedBy { it.calendarDay }
                .joinToString(",") { it.calendarDay.toString() }

            return AlarmEntity(
                id = alarm.id,
                hour = alarm.hour,
                minute = alarm.minute,
                label = alarm.label,
                repeatDays = repeatDaysStr,
                soundUri = alarm.soundUri,
                vibrationEnabled = alarm.vibrationEnabled,
                gradualVolumeEnabled = alarm.gradualVolumeEnabled,
                gradualVolumeDurationSeconds = alarm.gradualVolumeDurationSeconds,
                missionType = alarm.missionType.name,
                difficulty = alarm.difficulty.name,
                snoozeIntervalMinutes = alarm.snoozeIntervalMinutes,
                maxSnoozeCount = alarm.maxSnoozeCount,
                smartEscalationEnabled = alarm.smartEscalationEnabled,
                strictModeEnabled = alarm.strictModeEnabled,
                multiStepEnabled = alarm.multiStepEnabled,
                multiStepCount = alarm.multiStepCount,
                timedModeEnabled = alarm.timedModeEnabled,
                isActive = alarm.isActive,
                createdAt = alarm.createdAt,
                updatedAt = alarm.updatedAt
            )
        }
    }
}
