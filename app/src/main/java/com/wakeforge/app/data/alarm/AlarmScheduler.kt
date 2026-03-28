package com.wakeforge.app.data.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.domain.models.Alarm
import dagger.hilt.android.qualifiers.ApplicationContext
import com.wakeforge.app.domain.models.DayOfWeek as DomainDayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmDao: AlarmDao
) {

    companion object {
        private const val TAG = "AlarmScheduler"
        const val ALARM_ACTION_RING = "com.wakeforge.app.ACTION_ALARM_RING"
        const val EXTRA_ALARM_ID = "com.wakeforge.app.EXTRA_ALARM_ID"
    }

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(alarm: Alarm) {
        val nextFireTime = calculateNextFireTime(alarm) ?: return
        scheduleAlarmAtTime(alarm.id, nextFireTime)

        Log.d(
            TAG,
            "Scheduled alarm ${alarm.id} for ${LocalDateTime.ofInstant(nextFireTime, ZoneId.systemDefault())}"
        )
    }

    fun cancelAlarm(alarmId: String) {
        val pendingIntent = createPendingIntent(alarmId)
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled alarm $alarmId")
        }
    }

    suspend fun rescheduleAllActiveAlarms() {
        val activeAlarms = alarmDao.getActiveAlarmsOnce()
        Log.d(TAG, "Rescheduling ${activeAlarms.size} active alarms after boot")

        for (entity in activeAlarms) {
            val alarm = entity.toDomain()
            val nextFireTime = calculateNextFireTime(alarm) ?: continue
            scheduleAlarmAtTime(alarm.id, nextFireTime)
            Log.d(
                TAG,
                "Rescheduled alarm ${alarm.id} for ${
                    LocalDateTime.ofInstant(
                        nextFireTime,
                        ZoneId.systemDefault()
                    )
                }"
            )
        }
    }

    fun scheduleSnoozeAlarm(alarmId: String, snoozeIntervalMinutes: Int) {
        val triggerTime = System.currentTimeMillis() + snoozeIntervalMinutes * 60L * 1000L
        scheduleAlarmAtTime(alarmId, triggerTime)
        Log.d(TAG, "Scheduled snooze for alarm $alarmId in $snoozeIntervalMinutes minutes")
    }

    private fun scheduleAlarmAtTime(alarmId: String, triggerTimeMs: Long) {
        val pendingIntent = createPendingIntent(alarmId) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                    Log.w(TAG, "Exact alarm permission not granted, using inexact alarm")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule exact alarm", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    private fun calculateNextFireTime(alarm: Alarm): java.time.Instant? {
        val now = LocalDateTime.now()
        val alarmTime = LocalTime.of(alarm.hour, alarm.minute)

        if (alarm.repeatDays.isEmpty()) {
            val nextDateTime = if (now.toLocalTime().isBefore(alarmTime)) {
                now.toLocalDate().atTime(alarmTime)
            } else {
                now.toLocalDate().plusDays(1).atTime(alarmTime)
            }
            return nextDateTime.atZone(ZoneId.systemDefault()).toInstant()
        }

        val todayDayOfWeek = now.dayOfWeek
        val repeatDaysSet = alarm.repeatDays.toSet()

        for (i in 0..7) {
            val checkDate = now.toLocalDate().plusDays(i.toLong())
            val checkDay = checkDate.dayOfWeek
            val domainCheckDay = javaTimeToDomainDay(checkDay)

            if (domainCheckDay in repeatDaysSet) {
                val alarmDateTime = checkDate.atTime(alarmTime)

                if (i == 0 && now.toLocalTime().isAfter(alarmTime)) {
                    continue
                }

                if (i == 0 && now.toLocalTime().equals(alarmTime)) {
                    if (Duration.between(now, alarmDateTime).seconds < 60) {
                        continue
                    }
                }

                return alarmDateTime.atZone(ZoneId.systemDefault()).toInstant()
            }
        }

        val firstRepeatDay = alarm.repeatDays.minByOrNull { it.calendarDay } ?: return null
        val todayDomainDay = javaTimeToDomainDay(todayDayOfWeek)
        val daysUntilFirst = (firstRepeatDay.calendarDay - todayDomainDay.calendarDay + 7) % 7
        val nextDate = if (daysUntilFirst == 0L && now.toLocalTime().isAfter(alarmTime)) {
            now.toLocalDate().plusWeeks(1).atTime(alarmTime)
        } else {
            now.toLocalDate().plusDays(if (daysUntilFirst == 0L) 0 else daysUntilFirst.toLong())
                .atTime(alarmTime)
        }

        return nextDate.atZone(ZoneId.systemDefault()).toInstant()
    }

    private fun createPendingIntent(alarmId: String): PendingIntent? {
        val intent = Intent(ALARM_ACTION_RING).apply {
            putExtra(EXTRA_ALARM_ID, alarmId)
            setPackage(context.packageName)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getBroadcast(
            context,
            getRequestCode(alarmId),
            intent,
            flags
        )
    }

    private fun javaTimeToDomainDay(jtDay: java.time.DayOfWeek): DomainDayOfWeek {
        val calendarDay = when (jtDay) {
            java.time.DayOfWeek.MONDAY -> java.util.Calendar.MONDAY
            java.time.DayOfWeek.TUESDAY -> java.util.Calendar.TUESDAY
            java.time.DayOfWeek.WEDNESDAY -> java.util.Calendar.WEDNESDAY
            java.time.DayOfWeek.THURSDAY -> java.util.Calendar.THURSDAY
            java.time.DayOfWeek.FRIDAY -> java.util.Calendar.FRIDAY
            java.time.DayOfWeek.SATURDAY -> java.util.Calendar.SATURDAY
            java.time.DayOfWeek.SUNDAY -> java.util.Calendar.SUNDAY
        }
        return DomainDayOfWeek.from(calendarDay)
    }

    private fun getRequestCode(alarmId: String): Int {
        val hash = alarmId.hashCode()
        return if (hash == Int.MIN_VALUE) Int.MAX_VALUE else Math.abs(hash)
    }
}
