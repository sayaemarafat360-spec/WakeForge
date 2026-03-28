package com.wakeforge.app.data.repository

import com.wakeforge.app.data.database.dao.StreakDao
import com.wakeforge.app.data.database.dao.WakeRecordDao
import com.wakeforge.app.data.database.entity.StreakEntity
import com.wakeforge.app.data.database.entity.WakeRecordEntity
import com.wakeforge.app.domain.models.AnalyticsData
import com.wakeforge.app.domain.models.DailyStats
import com.wakeforge.app.domain.models.DayOfWeek
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.Streak
import com.wakeforge.app.domain.models.WakeOutcome
import com.wakeforge.app.domain.models.WakeRecord
import com.wakeforge.app.domain.repositories.StatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val wakeRecordDao: WakeRecordDao,
    private val streakDao: StreakDao
) : StatsRepository {

    override suspend fun recordWake(
        alarmId: String,
        outcome: WakeOutcome,
        snoozeCount: Int,
        missionType: MissionType,
        difficulty: MissionDifficulty,
        completionTimeMs: Long
    ) {
        val now = System.currentTimeMillis()
        val record = WakeRecord(
            id = UUID.randomUUID().toString(),
            alarmId = alarmId,
            timestamp = now,
            outcome = outcome,
            snoozeCount = snoozeCount,
            missionType = missionType,
            difficulty = difficulty,
            completionTimeMs = completionTimeMs,
            createdAt = now
        )

        val entity = WakeRecordEntity.fromDomain(record)
        wakeRecordDao.insertRecord(entity)

        ensureStreakExists()

        when (outcome) {
            WakeOutcome.SUCCESS -> {
                val newStreak = calculateNewStreak(now)
                streakDao.incrementSuccess(newStreak, now)
            }
            WakeOutcome.FAILURE -> {
                streakDao.recordFailure()
            }
            WakeOutcome.SNOOZE -> {
                streakDao.recordSnooze()
            }
        }
    }

    override fun getStreak(): Flow<Streak> {
        return streakDao.getStreak().map { entity ->
            entity?.toDomain() ?: createDefaultStreak()
        }
    }

    override suspend fun getStreakCount(): Int {
        val entity = streakDao.getStreakOnce()
        return entity?.currentStreak ?: 0
    }

    override suspend fun getWeeklyStats(): List<DailyStats> {
        val weekStart = getStartOfDaysAgo(7)
        val records = wakeRecordDao.getRecordsSince(weekStart).first()
        return groupRecordsByDay(records)
    }

    override suspend fun getAnalyticsData(): AnalyticsData {
        val now = System.currentTimeMillis()
        val weekStart = getStartOfDaysAgo(7)
        val monthStart = getStartOfDaysAgo(30)

        val streak = streakDao.getStreakOnce()
        val allRecords = wakeRecordDao.getAllRecords().first()

        val weekRecords = allRecords.filter { it.timestamp >= weekStart }
        val monthRecords = allRecords.filter { it.timestamp >= monthStart }

        val weeklySuccesses = weekRecords.count { it.outcome == WakeOutcome.SUCCESS.name }
        val weekTotalCount = weekRecords.size
        val monthlySuccesses = monthRecords.count { it.outcome == WakeOutcome.SUCCESS.name }
        val monthTotalCount = monthRecords.size

        val totalSuccesses = allRecords.count { it.outcome == WakeOutcome.SUCCESS.name }
        val totalFailures = allRecords.count { it.outcome == WakeOutcome.FAILURE.name }
        val totalSnoozes = allRecords.count { it.outcome == WakeOutcome.SNOOZE.name }
        val totalWakeUps = allRecords.size

        val weeklyData = groupRecordsByDay(weekRecords)

        val averageSnoozePerAlarm = if (totalWakeUps > 0) {
            allRecords.sumOf { it.snoozeCount }.toFloat() / totalWakeUps
        } else {
            0f
        }

        val mostUsedMissionType = allRecords
            .groupingBy { it.missionType }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.let { safeValueOf(MissionType::class.java, it) }

        val mostUsedDifficulty = allRecords
            .groupingBy { it.difficulty }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.let { safeValueOf(MissionDifficulty::class.java, it) }

        val bestDayOfWeek = computeBestDayOfWeek(allRecords)

        return AnalyticsData(
            currentStreak = streak?.currentStreak ?: 0,
            longestStreak = streak?.longestStreak ?: 0,
            weeklySuccessRate = calculateRate(weeklySuccesses, weekTotalCount),
            totalWakeUps = totalWakeUps,
            totalSnoozes = totalSnoozes,
            totalFailures = streak?.totalFailures ?: totalFailures,
            averageSnoozePerAlarm = averageSnoozePerAlarm,
            mostUsedMissionType = mostUsedMissionType,
            mostUsedDifficulty = mostUsedDifficulty,
            weeklyData = weeklyData,
            monthlySuccessRate = calculateRate(monthlySuccesses, monthTotalCount),
            bestDayOfWeek = bestDayOfWeek
        )
    }

    override suspend fun getSuccessRate(period: StatsRepository.Period): Float {
        val startDate = when (period) {
            StatsRepository.Period.WEEK -> getStartOfDaysAgo(7)
            StatsRepository.Period.MONTH -> getStartOfDaysAgo(30)
            StatsRepository.Period.ALL_TIME -> 0L
        }
        val successCount = wakeRecordDao.getSuccessCountSince(startDate)
        val totalCount = wakeRecordDao.getTotalCountSince(startDate)
        return calculateRate(successCount, totalCount)
    }

    override suspend fun getTotalWakeUps(): Int {
        return wakeRecordDao.getTotalCountSince(0L)
    }

    private suspend fun ensureStreakExists() {
        val existing = streakDao.getStreakOnce()
        if (existing == null) {
            streakDao.updateStreak(StreakEntity.createDefault())
        }
    }

    private suspend fun calculateNewStreak(now: Long): Int {
        val streak = streakDao.getStreakOnce() ?: return 1
        val lastSuccess = streak.lastSuccessDate ?: return 1

        val lastDate = LocalDate.ofInstant(
            Instant.ofEpochMilli(lastSuccess),
            ZoneId.systemDefault()
        )
        val currentDate = LocalDate.ofInstant(
            Instant.ofEpochMilli(now),
            ZoneId.systemDefault()
        )

        val daysBetween = ChronoUnit.DAYS.between(lastDate, currentDate)

        return when {
            daysBetween == 0L -> streak.currentStreak
            daysBetween == 1L -> streak.currentStreak + 1
            else -> 1
        }
    }

    private fun createDefaultStreak(): Streak {
        return StreakEntity.createDefault().toDomain()
    }

    private fun groupRecordsByDay(entities: List<WakeRecordEntity>): List<DailyStats> {
        return entities
            .groupBy { entity ->
                LocalDate.ofInstant(
                    Instant.ofEpochMilli(entity.timestamp),
                    ZoneId.systemDefault()
                )
            }
            .map { (date, records) ->
                val successes = records.count { it.outcome == WakeOutcome.SUCCESS.name }
                val failures = records.count { it.outcome == WakeOutcome.FAILURE.name }
                val snoozes = records.count { it.outcome == WakeOutcome.SNOOZE.name }

                DailyStats(
                    date = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000,
                    dayOfWeek = isoToCalendarDay(date.dayOfWeek.value),
                    successes = successes,
                    failures = failures,
                    snoozes = snoozes
                )
            }
            .sortedByDescending { it.date }
    }

    private fun computeBestDayOfWeek(records: List<WakeRecordEntity>): DayOfWeek? {
        if (records.isEmpty()) return null

        val dayStats = records.groupBy { entity ->
            isoToCalendarDay(
                LocalDate.ofInstant(
                    Instant.ofEpochMilli(entity.timestamp),
                    ZoneId.systemDefault()
                ).dayOfWeek.value
            )
        }.map { (calendarDay, dayRecords) ->
            val successes = dayRecords.count { it.outcome == WakeOutcome.SUCCESS.name }
            val total = dayRecords.size
            val rate = if (total > 0) successes.toFloat() / total else 0f
            calendarDay to rate
        }

        val best = dayStats.maxByOrNull { it.second } ?: return null

        return try {
            DayOfWeek.from(best.first)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun getStartOfDaysAgo(days: Int, referenceTimestamp: Long? = null): Long {
        val ref = referenceTimestamp ?: System.currentTimeMillis()
        val date = LocalDate.ofInstant(
            Instant.ofEpochMilli(ref),
            ZoneId.systemDefault()
        ).minusDays(days.toLong())
        return date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond() * 1000
    }

    private fun isoToCalendarDay(isoDay: Int): Int {
        return (isoDay % 7) + 1
    }

    private fun calculateRate(successCount: Int, totalCount: Int): Float {
        if (totalCount == 0) return 0f
        return successCount.toFloat() / totalCount.toFloat()
    }

    private inline fun <reified T : Enum<T>> safeValueOf(enumClass: KClass<T>, name: String): T? {
        return try {
            java.lang.Enum.valueOf(enumClass.java, name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
