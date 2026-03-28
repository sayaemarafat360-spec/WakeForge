package com.wakeforge.app.domain.models

import java.util.Calendar
import java.util.UUID

/**
 * Represents a single alarm configured by the user.
 *
 * @property id Unique identifier generated via UUID.
 * @property hour Hour of day in 24-hour format (0–23).
 * @property minute Minute of the hour (0–59).
 * @property label User-facing name for the alarm.
 * @property repeatDays Days of the week on which the alarm repeats.
 * @property soundUri URI or identifier for the alarm sound.
 * @property vibrationEnabled Whether vibration accompanies the alarm sound.
 * @property gradualVolumeEnabled Whether volume ramps up gradually.
 * @property gradualVolumeDurationSeconds Duration over which volume increases.
 * @property missionType The type of mission required to dismiss the alarm.
 * @property difficulty Difficulty tier for the dismissal mission.
 * @property snoozeIntervalMinutes Minutes between snooze activations.
 * @property maxSnoozeCount Maximum number of snoozes allowed.
 * @property smartEscalationEnabled Whether mission difficulty increases with each snooze.
 * @property strictModeEnabled If true, the alarm cannot be dismissed without completing the mission.
 * @property multiStepEnabled Whether multiple missions must be completed sequentially.
 * @property multiStepCount Number of missions in a multi-step chain.
 * @property timedModeEnabled Whether the mission has a time limit.
 * @property isActive Whether the alarm is currently enabled.
 * @property createdAt Timestamp when the alarm was first created.
 * @property updatedAt Timestamp of the last modification.
 */
data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val repeatDays: List<DayOfWeek> = emptyList(),
    val soundUri: String = "builtin_dawn",
    val vibrationEnabled: Boolean = true,
    val gradualVolumeEnabled: Boolean = false,
    val gradualVolumeDurationSeconds: Int = 60,
    val missionType: MissionType = MissionType.MATH,
    val difficulty: MissionDifficulty = MissionDifficulty.MEDIUM,
    val snoozeIntervalMinutes: Int = 5,
    val maxSnoozeCount: Int = 3,
    val smartEscalationEnabled: Boolean = true,
    val strictModeEnabled: Boolean = false,
    val multiStepEnabled: Boolean = false,
    val multiStepCount: Int = 2,
    val timedModeEnabled: Boolean = true,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {

    /**
     * Calculates the next fire time based on the current time and repeat schedule.
     *
     * @return A [Calendar] instance set to the next alarm fire time.
     */
    fun nextFireTime(): Calendar {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val now = Calendar.getInstance()

        // If the alarm time has already passed today, move to tomorrow.
        if (calendar.before(now) || calendar == now) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // If repeating days are set, find the next matching day.
        if (repeatDays.isNotEmpty()) {
            val maxIterations = 7
            var iterations = 0
            while (!repeatDays.contains(DayOfWeek.from(calendar.get(Calendar.DAY_OF_WEEK))) && iterations < maxIterations) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                iterations++
            }
        }

        return calendar
    }

    /**
     * Returns the alarm time formatted as a 12-hour or 24-hour string.
     */
    fun formattedTime(): String {
        val isPm = hour >= 12
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        val minuteStr = minute.toString().padStart(2, '0')
        return "$displayHour:$minuteStr"
    }

    /**
     * Returns a human-readable summary of the repeat days.
     * E.g., "Mon, Tue, Wed" or "Once" for non-repeating alarms.
     */
    fun repeatDaysText(): String {
        return if (repeatDays.isEmpty()) {
            "Once"
        } else if (repeatDays.size == 7) {
            "Every day"
        } else if (repeatDays.size == 5 &&
            repeatDays.containsAll(listOf(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI))
        ) {
            "Weekdays"
        } else if (repeatDays.size == 2 &&
            repeatDays.contains(DayOfWeek.SAT) &&
            repeatDays.contains(DayOfWeek.SUN)
        ) {
            "Weekends"
        } else {
            repeatDays.sortedBy { it.calendarDay }.joinToString(", ") { it.abbreviation }
        }
    }

    /**
     * Returns true if the alarm has at least one repeat day configured.
     */
    fun isRepeating(): Boolean = repeatDays.isNotEmpty()
}

/**
 * Represents a day of the week, mapped to [Calendar] constants.
 */
enum class DayOfWeek(val calendarDay: Int, val abbreviation: String) {
    SUN(Calendar.SUNDAY, "Sun"),
    MON(Calendar.MONDAY, "Mon"),
    TUE(Calendar.TUESDAY, "Tue"),
    WED(Calendar.WEDNESDAY, "Wed"),
    THU(Calendar.THURSDAY, "Thu"),
    FRI(Calendar.FRIDAY, "Fri"),
    SAT(Calendar.SATURDAY, "Sat");

    companion object {
        /**
         * Maps a [Calendar] day-of-week constant to a [DayOfWeek] enum value.
         */
        fun from(calendarDay: Int): DayOfWeek {
            return entries.firstOrNull { it.calendarDay == calendarDay }
                ?: throw IllegalArgumentException("Invalid calendar day: $calendarDay")
        }
    }
}
