package com.wakeforge.app.core.utils

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

/**
 * Utility functions for time and alarm scheduling calculations.
 */
object TimeUtils {

    // ────────────────────────────────────────────────────────────────────────
    // Next Alarm Calculation
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Calculates the next [Calendar] instance at which the alarm should fire.
     *
     * Rules:
     * - If [repeatDays] is empty the alarm fires once — today if the time hasn't
     *   passed yet, otherwise tomorrow.
     * - If [repeatDays] is non-empty the alarm fires on the next matching day
     *   (including today if the time hasn't passed).
     *
     * @param hour       Hour of day (0–23).
     * @param minute     Minute of hour (0–59).
     * @param repeatDays Days the alarm repeats on; empty = one-time alarm.
     * @return A [Calendar] set to the next alarm fire time.
     */
    fun calculateNextAlarmTime(
        hour: Int,
        minute: Int,
        repeatDays: List<DayOfWeek>,
    ): Calendar {
        val now = Calendar.getInstance()
        val candidate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return if (repeatDays.isEmpty()) {
            // One-time alarm
            if (candidate.before(now) || candidate == now) {
                candidate.add(Calendar.DAY_OF_MONTH, 1)
            }
            candidate
        } else {
            // Repeating alarm — find the next matching day
            findNextMatchingDay(candidate, now, repeatDays)
        }
    }

    /**
     * Starting from [candidate], advances day-by-day until it finds a day that
     * is both in [repeatDays] and not in the past relative to [now].
     */
    private fun findNextMatchingDay(
        candidate: Calendar,
        now: Calendar,
        repeatDays: List<DayOfWeek>,
    ): Calendar {
        // Convert DayOfWeek to Calendar day constants (Calendar.SUNDAY = 1 … SATURDAY = 7)
        val calendarDays = repeatDays.map { it.toCalendarDay() }.toSet()

        // Check up to 7 days ahead
        repeat(7) { _ ->
            val candidateDay = candidate.get(Calendar.DAY_OF_WEEK)
            if (candidateDay in calendarDays && candidate.after(now)) {
                return candidate
            }
            candidate.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Fallback — shouldn't happen if repeatDays is valid
        return candidate
    }

    /**
     * Returns the number of milliseconds from now until [alarmTime].
     * Returns 0 if the alarm time is in the past.
     */
    fun getTimeUntilAlarm(alarmTime: Calendar): Long {
        val now = Calendar.getInstance()
        val diff = alarmTime.timeInMillis - now.timeInMillis
        return if (diff > 0) diff else 0L
    }

    // ────────────────────────────────────────────────────────────────────────
    // Today / Tomorrow checks
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns `true` if the given [hour]:[minute] is today and hasn't passed yet.
     */
    fun isAlarmToday(hour: Int, minute: Int): Boolean {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return alarm.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
            alarm.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            alarm.after(now)
    }

    /**
     * Returns `true` if the given [hour]:[minute] falls on tomorrow's date.
     */
    fun isAlarmTomorrow(hour: Int, minute: Int): Boolean {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // If alarm is today and hasn't passed, it's "today", not "tomorrow"
        if (isAlarmToday(hour, minute)) return false

        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }
        return alarm.get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR) &&
            alarm.get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR)
    }

    // ────────────────────────────────────────────────────────────────────────
    // Countdown formatting
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Formats a duration in milliseconds into a compact countdown string.
     *
     * - >= 1 hour:   "Xh Ym"   (e.g. "8h 30m")
     * - < 1 hour:    "Xm Ys"   (e.g. "45m 12s")
     * - < 1 minute:  "Ys"      (e.g. "45s")
     */
    fun formatCountdown(ms: Long): String {
        val totalSeconds = ms / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Day of week helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current day of the week as a [DayOfWeek].
     */
    fun getCurrentDayOfWeek(): DayOfWeek {
        return LocalDate.now().dayOfWeek
    }

    /**
     * Returns `true` if the system is configured to use 24-hour time format.
     */
    fun is24HourFormat(context: Context): Boolean {
        return android.text.format.DateFormat.is24HourFormat(context)
    }

    // ────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Maps java.time [DayOfWeek] to `java.util.Calendar` day-of-week constant.
     * Calendar.SUNDAY = 1, … Calendar.SATURDAY = 7.
     */
    internal fun DayOfWeek.toCalendarDay(): Int = when (this) {
        DayOfWeek.SUNDAY    -> Calendar.SUNDAY
        DayOfWeek.MONDAY    -> Calendar.MONDAY
        DayOfWeek.TUESDAY   -> Calendar.TUESDAY
        DayOfWeek.WEDNESDAY -> Calendar.WEDNESDAY
        DayOfWeek.THURSDAY  -> Calendar.THURSDAY
        DayOfWeek.FRIDAY    -> Calendar.FRIDAY
        DayOfWeek.SATURDAY  -> Calendar.SATURDAY
    }
}
