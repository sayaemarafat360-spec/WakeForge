package com.wakeforge.app.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wakeforge.app.data.database.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streaks WHERE id = 'main'")
    fun getStreak(): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE id = 'main'")
    suspend fun getStreakOnce(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateStreak(streak: StreakEntity)

    @Query(
        """
        UPDATE streaks 
        SET totalSuccesses = totalSuccesses + 1,
            currentStreak = :newCurrentStreak,
            longestStreak = CASE WHEN :newCurrentStreak > longestStreak THEN :newCurrentStreak ELSE longestStreak END,
            lastSuccessDate = :timestamp
        WHERE id = 'main'
        """
    )
    suspend fun incrementSuccess(newCurrentStreak: Int, timestamp: Long)

    @Query(
        """
        UPDATE streaks 
        SET currentStreak = 0, totalFailures = totalFailures + 1 
        WHERE id = 'main'
        """
    )
    suspend fun recordFailure()

    @Query(
        """
        UPDATE streaks 
        SET totalSnoozes = totalSnoozes + 1 
        WHERE id = 'main'
        """
    )
    suspend fun recordSnooze()
}
