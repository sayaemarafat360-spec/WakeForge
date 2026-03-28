package com.wakeforge.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wakeforge.app.data.database.converter.Converters
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.data.database.dao.StreakDao
import com.wakeforge.app.data.database.dao.WakeRecordDao
import com.wakeforge.app.data.database.entity.AlarmEntity
import com.wakeforge.app.data.database.entity.StreakEntity
import com.wakeforge.app.data.database.entity.WakeRecordEntity
import javax.inject.Singleton

@Database(
    entities = [
        AlarmEntity::class,
        WakeRecordEntity::class,
        StreakEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WakeForgeDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    abstract fun wakeRecordDao(): WakeRecordDao

    abstract fun streakDao(): StreakDao
}
