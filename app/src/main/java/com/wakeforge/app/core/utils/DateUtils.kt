import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material3.rememberDismissState
package com.wakeforge.app.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Utility functions for date comparison and formatting.
 */
object DateUtils {

    // â”€â”€ Date Formats â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private val shortDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val mediumDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val dayHeaderFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Date Comparison
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Returns `true` if two timestamps fall on the same calendar day
     * (year + day-of-year match).
     */
    fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = ts1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = ts2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Returns `true` if [ts] falls within the current calendar week.
     * The week starts on Monday (ISO 8601).
     */
    fun isSameWeek(ts: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        val now = Calendar.getInstance()

        // Align both calendars to the Monday of their respective weeks
        val calWeekStart = getMondayOfWeek(cal)
        val nowWeekStart = getMondayOfWeek(now)

        return calWeekStart.get(Calendar.YEAR) == nowWeekStart.get(Calendar.YEAR) &&
            calWeekStart.get(Calendar.DAY_OF_YEAR) == nowWeekStart.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Returns `true` if [ts] falls within the current calendar month.
     */
    fun isSameMonth(ts: Long): Boolean {
        val cal = Calendar.getInstance().apply { timeInMillis = ts }
        val now = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
            cal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Week / Month ranges
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Returns the start of the current week (Monday 00:00:00.000) as a timestamp.
     */
    fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        return getMondayOfWeek(cal).timeInMillis
    }

    /**
     * Returns a list of 7 timestamps representing the start of each day in the
     * current week (Monday through Sunday).
     */
    fun getDaysInWeek(): List<Long> {
        val monday = getMondayOfWeek(Calendar.getInstance())
        return (0 until 7).map { offset ->
            val day = monday.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, offset)
            day.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    /**
     * Returns a list of timestamps representing the start of each day in the
     * current month.
     */
    fun getDaysInMonth(): List<Long> {
        val now = Calendar.getInstance()
        val daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..daysInMonth).map { day ->
            Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Formatting
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Formats a timestamp into a human-readable date string.
     *
     * - Same year  â†’ "Jan 15"
     * - Different  â†’ "Jan 15, 2024"
     */
    fun formatDate(timestamp: Long): String {
        val now = Calendar.getInstance()
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }

        return if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
            shortDateFormat.format(Date(timestamp))
        } else {
            mediumDateFormat.format(Date(timestamp))
        }
    }

    /**
     * Returns the day-of-week for the given timestamp as an integer
     * where 1 = Sunday, 2 = Monday, â€¦ 7 = Saturday (Calendar convention).
     */
    fun getDayOfWeek(timestamp: Long): Int {
        return Calendar.getInstance()
            .apply { timeInMillis = timestamp }
            .get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Returns the full day name for a timestamp (e.g. "Monday").
     */
    fun getFullDayName(timestamp: Long): String {
        return dayHeaderFormat.format(Date(timestamp)).split(", ").first()
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // Internal helpers
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    /**
     * Adjusts the given [Calendar] to the Monday of its week, midnight.
     */
    private fun getMondayOfWeek(cal: Calendar): Calendar {
        val clone = cal.clone() as Calendar
        val day = clone.get(Calendar.DAY_OF_WEEK)
        // Calendar.SUNDAY = 1; shift so that Monday = 0 offset
        val offset = if (day == Calendar.SUNDAY) -6 else (Calendar.MONDAY - day)
        clone.add(Calendar.DAY_OF_MONTH, offset)
        clone.set(Calendar.HOUR_OF_DAY, 0)
        clone.set(Calendar.MINUTE, 0)
        clone.set(Calendar.SECOND, 0)
        clone.set(Calendar.MILLISECOND, 0)
        return clone
    }
}

