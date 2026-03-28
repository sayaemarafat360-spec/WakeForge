package com.wakeforge.app.di

import com.wakeforge.app.data.repository.AlarmRepositoryImpl
import com.wakeforge.app.data.repository.MissionRepositoryImpl
import com.wakeforge.app.data.repository.PremiumRepositoryImpl
import com.wakeforge.app.data.repository.SettingsRepositoryImpl
import com.wakeforge.app.data.repository.StatsRepositoryImpl
import com.wakeforge.app.domain.repositories.AlarmRepository
import com.wakeforge.app.domain.repositories.MissionRepository
import com.wakeforge.app.domain.repositories.PremiumRepository
import com.wakeforge.app.domain.repositories.SettingsRepository
import com.wakeforge.app.domain.repositories.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds each domain repository **interface** to its
 * concrete **implementation**.
 *
 * Using `@Binds` is more efficient than `@Provides` because Hilt can
 * generate the binding without an intermediate factory class.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds [AlarmRepository] to [AlarmRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        impl: AlarmRepositoryImpl
    ): AlarmRepository

    /**
     * Binds [MissionRepository] to [MissionRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindMissionRepository(
        impl: MissionRepositoryImpl
    ): MissionRepository

    /**
     * Binds [StatsRepository] to [StatsRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindStatsRepository(
        impl: StatsRepositoryImpl
    ): StatsRepository

    /**
     * Binds [SettingsRepository] to [SettingsRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    /**
     * Binds [PremiumRepository] to [PremiumRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        impl: PremiumRepositoryImpl
    ): PremiumRepository
}
