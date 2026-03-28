package com.wakeforge.app.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wakeforge.app.domain.models.MissionDifficulty
import com.wakeforge.app.domain.models.MissionType
import com.wakeforge.app.domain.models.WakeOutcome
import com.wakeforge.app.domain.models.WakeRecord

@Entity(
    tableName = "wake_records",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["alarmId"]),
        Index(value = ["timestamp"]),
        Index(value = ["outcome"])
    ]
)
data class WakeRecordEntity(
    @PrimaryKey
    val id: String,
    val alarmId: String,
    val timestamp: Long,
    val outcome: String,
    val snoozeCount: Int,
    val missionType: String,
    val difficulty: String,
    val completionTimeMs: Long,
    val createdAt: Long
) {

    fun toDomain(): WakeRecord {
        return WakeRecord(
            id = id,
            alarmId = alarmId,
            timestamp = timestamp,
            outcome = try {
                WakeOutcome.valueOf(outcome)
            } catch (e: IllegalArgumentException) {
                WakeOutcome.FAILURE
            },
            snoozeCount = snoozeCount,
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
            completionTimeMs = completionTimeMs,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomain(record: WakeRecord): WakeRecordEntity {
            return WakeRecordEntity(
                id = record.id,
                alarmId = record.alarmId,
                timestamp = record.timestamp,
                outcome = record.outcome.name,
                snoozeCount = record.snoozeCount,
                missionType = record.missionType.name,
                difficulty = record.difficulty.name,
                completionTimeMs = record.completionTimeMs,
                createdAt = record.createdAt
            )
        }
    }
}
