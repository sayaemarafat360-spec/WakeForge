package com.wakeforge.app.data.database.converter

import androidx.room.TypeConverter
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.WakeOutcome
import com.wakeforge.app.domain.models.DayOfWeek

class Converters {

    @TypeConverter
    fun fromDayOfWeekList(list: List<DayOfWeek>): String {
        return list
            .sortedBy { it.calendarDay }
            .joinToString(",") { it.calendarDay.toString() }
    }

    @TypeConverter
    fun toDayOfWeekList(value: String): List<DayOfWeek> {
        if (value.isBlank()) return emptyList()
        return value
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { dayStr ->
                dayStr.toIntOrNull()?.let { dayIndex ->
                    try {
                        DayOfWeek.entries.firstOrNull { it.calendarDay == dayIndex }
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
            }
    }

    @TypeConverter
    fun fromMissionType(type: MissionType): String {
        return type.name
    }

    @TypeConverter
    fun toMissionType(value: String): MissionType {
        return try {
            MissionType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MissionType.MATH
        }
    }

    @TypeConverter
    fun fromMissionDifficulty(diff: MissionDifficulty): String {
        return diff.name
    }

    @TypeConverter
    fun toMissionDifficulty(value: String): MissionDifficulty {
        return try {
            MissionDifficulty.valueOf(value)
        } catch (e: IllegalArgumentException) {
            MissionDifficulty.MEDIUM
        }
    }

    @TypeConverter
    fun fromWakeOutcome(outcome: WakeOutcome): String {
        return outcome.name
    }

    @TypeConverter
    fun toWakeOutcome(value: String): WakeOutcome {
        return try {
            WakeOutcome.valueOf(value)
        } catch (e: IllegalArgumentException) {
            WakeOutcome.FAILURE
        }
    }
}
