package com.wakeforge.app.core.extensions

import android.content.Context
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

// ──────────────────────────────────────────────────────────────────────────────
// Int → Formatted Time / Duration
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Converts an [Int] representing total minutes-since-midnight to a formatted
 * time string.
 *
 * @param is24Hour If `true` uses 24-hour format (e.g. "14:30").
 *                 If `false` uses 12-hour format (e.g. "2:30 PM").
 * @receiver Total minutes since midnight (0..1439).
 */
fun Int.toFormattedTime(is24Hour: Boolean = false): String {
    require(this in 0..1439) { "Value must be in 0..1439 (minutes since midnight), got $this" }
    val hours = this / 60
    val minutes = this % 60
    return if (is24Hour) {
        String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    } else {
        val amPm = if (hours >= 12) "PM" else "AM"
        val displayHours = if (hours == 0) 12 else if (hours > 12) hours - 12 else hours
        String.format(Locale.getDefault(), "%d:%02d %s", displayHours, minutes, amPm)
    }
}

/**
 * Converts a total number of seconds into a human-readable duration string.
 *
 * Examples:
 * - 3661 → "1h 1m"
 * - 125   → "2m 5s"
 * - 45    → "45s"
 *
 * @receiver Duration in seconds.
 */
fun Int.toFormattedDuration(): String {
    val totalSeconds = this
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        if (seconds > 0 || hours == 0 && minutes == 0) append("${seconds}s")
    }.trim()
}

// ──────────────────────────────────────────────────────────────────────────────
// Long (timestamp) → Formatted Date / Time
// ──────────────────────────────────────────────────────────────────────────────

private val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())

private val timeFormat: DateFormat
    get() = SimpleDateFormat("hh:mm a", Locale.getDefault())

/**
 * Converts a Unix epoch millis timestamp to a formatted date string
 * using the device's locale (e.g. "Jan 15, 2025").
 */
fun Long.toDateString(): String = dateFormat.format(Date(this))

/**
 * Converts a Unix epoch millis timestamp to a formatted time string
 * (e.g. "2:30 PM").
 */
fun Long.toTimeString(): String = timeFormat.format(Date(this))

// ──────────────────────────────────────────────────────────────────────────────
// DayOfWeek helpers
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Maps a Java [java.time.DayOfWeek] to a 3-letter short name (e.g. "Mon").
 */
fun java.time.DayOfWeek.toShortName(): String = when (this) {
    java.time.DayOfWeek.MONDAY    -> "Mon"
    java.time.DayOfWeek.TUESDAY   -> "Tue"
    java.time.DayOfWeek.WEDNESDAY -> "Wed"
    java.time.DayOfWeek.THURSDAY  -> "Thu"
    java.time.DayOfWeek.FRIDAY    -> "Fri"
    java.time.DayOfWeek.SATURDAY  -> "Sat"
    java.time.DayOfWeek.SUNDAY    -> "Sun"
}

/**
 * Converts a list of [java.time.DayOfWeek] into a comma-separated repeat label.
 *
 * Examples:
 * - [MON, WED, FRI]           → "Mon, Wed, Fri"
 * - [MON,TUE,WED,THU,FRI]    → "Weekdays"
 * - [SAT, SUN]                → "Weekends"
 * - all 7 days                → "Every day"
 * - empty list                → "Once"
 */
fun List<java.time.DayOfWeek>.toRepeatLabel(): String {
    if (isEmpty()) return "Once"
    if (size == 7) return "Every day"

    val weekdays = listOf(
        java.time.DayOfWeek.MONDAY,
        java.time.DayOfWeek.TUESDAY,
        java.time.DayOfWeek.WEDNESDAY,
        java.time.DayOfWeek.THURSDAY,
        java.time.DayOfWeek.FRIDAY,
    )
    val weekends = listOf(
        java.time.DayOfWeek.SATURDAY,
        java.time.DayOfWeek.SUNDAY,
    )

    if (containsAll(weekdays) && !containsAll(weekends)) return "Weekdays"
    if (containsAll(weekends) && !intersects(weekdays)) return "Weekends"

    return joinToString(", ") { it.toShortName() }
}

/**
 * Converts a bitmask (as used by Android's Calendar) into a list of
 * [java.time.DayOfWeek] values.
 *
 * Android convention: bit 0 = Sunday, bit 1 = Monday, … bit 6 = Saturday.
 *
 * @receiver Bitmask where set bits represent active days.
 */
fun Int.toDayList(): List<java.time.DayOfWeek> {
    val calendarDays = listOf(
        java.time.DayOfWeek.SUNDAY,
        java.time.DayOfWeek.MONDAY,
        java.time.DayOfWeek.TUESDAY,
        java.time.DayOfWeek.WEDNESDAY,
        java.time.DayOfWeek.THURSDAY,
        java.time.DayOfWeek.FRIDAY,
        java.time.DayOfWeek.SATURDAY,
    )
    return calendarDays.mapIndexedNotNull { index, dayOfWeek ->
        if ((this and (1 shl index)) != 0) dayOfWeek else null
    }
}

/**
 * Converts a list of [java.time.DayOfWeek] into a bitmask matching Android's
 * Calendar convention (bit 0 = Sunday).
 */
fun List<java.time.DayOfWeek>.toBitMask(): Int {
    return fold(0) { mask, day ->
        val bit = when (day) {
            java.time.DayOfWeek.SUNDAY    -> 0
            java.time.DayOfWeek.MONDAY    -> 1
            java.time.DayOfWeek.TUESDAY   -> 2
            java.time.DayOfWeek.WEDNESDAY -> 3
            java.time.DayOfWeek.THURSDAY  -> 4
            java.time.DayOfWeek.FRIDAY    -> 5
            java.time.DayOfWeek.SATURDAY  -> 6
        }
        mask or (1 shl bit)
    }
}

private fun <E> List<E>.intersects(other: List<E>): Boolean = any { it in other }

// ──────────────────────────────────────────────────────────────────────────────
// Calendar helpers
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Returns `true` if this [Calendar] represents today's date.
 */
fun Calendar.isToday(): Boolean {
    val now = Calendar.getInstance()
    return get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
}

/**
 * Returns `true` if this [Calendar] represents tomorrow's date.
 */
fun Calendar.isTomorrow(): Boolean {
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1) }
    return get(Calendar.YEAR) == tomorrow.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == tomorrow.get(Calendar.DAY_OF_YEAR)
}

// ──────────────────────────────────────────────────────────────────────────────
// Relative time string
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Converts a Unix epoch millis timestamp into a human-readable relative time string.
 *
 * Examples (in English):
 * - "Just now"       (0 s ago)
 * - "5m ago"         (5 minutes ago)
 * - "2h ago"         (2 hours ago)
 * - "Yesterday"      (between 24h and 48h ago)
 * - "Jan 15, 2025"   (older than 48h)
 */
fun Long.toRelativeTimeString(context: Context): String {
    val now = System.currentTimeMillis()
    val diffMs = now - this
    val diffSeconds = diffMs / 1000
    val diffMinutes = diffSeconds / 60
    val diffHours = diffMinutes / 60
    val diffDays = diffHours / 24

    return when {
        diffSeconds < 60   -> "Just now"
        diffMinutes < 60   -> "${diffMinutes}m ago"
        diffHours < 24     -> "${diffHours}h ago"
        diffDays == 1L     -> "Yesterday"
        diffDays < 7       -> toDateString()
        else               -> toDateString()
    }
}
