package com.wakeforge.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wakeforge.app.domain.models.Streak

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey
    val id: String = "main",
    val currentStreak: Int,
    val longestStreak: Int,
    val lastSuccessDate: Long?,
    val totalSuccesses: Int,
    val totalFailures: Int,
    val totalSnoozes: Int
) {

    fun toDomain(): Streak {
        return Streak(
            id = id,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            lastSuccessDate = lastSuccessDate,
            totalSuccesses = totalSuccesses,
            totalFailures = totalFailures,
            totalSnoozes = totalSnoozes
        )
    }

    companion object {
        fun fromDomain(streak: Streak): StreakEntity {
            return StreakEntity(
                id = streak.id,
                currentStreak = streak.currentStreak,
                longestStreak = streak.longestStreak,
                lastSuccessDate = streak.lastSuccessDate,
                totalSuccesses = streak.totalSuccesses,
                totalFailures = streak.totalFailures,
                totalSnoozes = streak.totalSnoozes
            )
        }

        fun createDefault(): StreakEntity {
            return StreakEntity(
                id = "main",
                currentStreak = 0,
                longestStreak = 0,
                lastSuccessDate = null,
                totalSuccesses = 0,
                totalFailures = 0,
                totalSnoozes = 0
            )
        }
    }
}
