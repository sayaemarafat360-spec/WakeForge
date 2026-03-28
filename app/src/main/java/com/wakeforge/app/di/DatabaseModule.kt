package com.wakeforge.app.di

import android.content.Context
import androidx.room.Room
import com.wakeforge.app.data.datastore.SettingsDataStore
import com.wakeforge.app.data.database.WakeForgeDatabase
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.data.database.dao.StreakDao
import com.wakeforge.app.data.database.dao.WakeRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module responsible for providing the Room database instance,
 * individual DAOs, and the [SettingsDataStore].
 *
 * All provision is scoped to the [SingletonComponent] (application lifetime).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "wakeforge.db"

    /**
     * Creates and provides the [WakeForgeDatabase] singleton.
     *
     * Uses [Room.databaseBuilder] with fallback-to-destructive-migration so the
     * app does not crash when the schema version changes during development.
     * In production you would swap this for a proper [Migration] strategy.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WakeForgeDatabase {
        return Room.databaseBuilder(
            context,
            WakeForgeDatabase::class.java,
            DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Exposes the [AlarmDao] from the database instance.
     */
    @Provides
    fun provideAlarmDao(database: WakeForgeDatabase): AlarmDao {
        return database.alarmDao()
    }

    /**
     * Exposes the [WakeRecordDao] from the database instance.
     */
    @Provides
    fun provideWakeRecordDao(database: WakeForgeDatabase): WakeRecordDao {
        return database.wakeRecordDao()
    }

    /**
     * Exposes the [StreakDao] from the database instance.
     */
    @Provides
    fun provideStreakDao(database: WakeForgeDatabase): StreakDao {
        return database.streakDao()
    }

    /**
     * Provides the [SettingsDataStore] singleton.
     *
     * [SettingsDataStore] already carries its own `@Inject constructor` so Hilt
     * can create it directly; we simply wire it here for discoverability and
     * to ensure the [ApplicationContext] qualifier is used.
     */
    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context
    ): SettingsDataStore {
        return SettingsDataStore(context)
    }
}
