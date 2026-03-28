package com.wakeforge.app.di

import android.content.Context
import com.wakeforge.app.data.ad.AdManager
import com.wakeforge.app.data.alarm.AlarmScheduler
import com.wakeforge.app.data.database.dao.AlarmDao
import com.wakeforge.app.data.mission.MissionEngine
import com.wakeforge.app.data.mission.generators.MathGenerator
import com.wakeforge.app.data.mission.generators.MemoryGenerator
import com.wakeforge.app.data.mission.generators.PhraseGenerator
import com.wakeforge.app.data.mission.generators.ShakeEvaluator
import com.wakeforge.app.data.mission.generators.StepEvaluator
import com.wakeforge.app.data.premium.PremiumManager
import com.wakeforge.app.data.sound.SoundManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides service-layer singletons:
 * [AlarmScheduler], [SoundManager], [MissionEngine], and [AdManager].
 *
 * Each provider returns the concrete class directly. Where the class
 * already carries an `@Inject constructor` Hilt could satisfy the
 * dependency automatically; however, explicit providers give us
 * compile-time visibility into the dependency graph and make testing
 * easier via module replacement.
 */
@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    /**
     * Provides the [AlarmScheduler] singleton.
     *
     * The scheduler requires an [ApplicationContext] and [AlarmDao] so it can
     * reschedule alarms after device reboot.
     */
    @Provides
    @Singleton
    fun provideAlarmScheduler(
        @ApplicationContext context: Context,
        alarmDao: AlarmDao
    ): AlarmScheduler {
        return AlarmScheduler(context, alarmDao)
    }

    /**
     * Provides the [SoundManager] singleton.
     *
     * The sound manager needs application context to resolve raw resource URIs
     * and to access system audio services.
     */
    @Provides
    @Singleton
    fun provideSoundManager(
        @ApplicationContext context: Context
    ): SoundManager {
        return SoundManager(context)
    }

    /**
     * Provides the [MissionEngine] singleton.
     *
     * All five mission generators are injected so the engine can dispatch to
     * the correct one based on [com.wakeforge.app.domain.models.MissionType].
     */
    @Provides
    @Singleton
    fun provideMissionEngine(
        mathGenerator: MathGenerator,
        memoryGenerator: MemoryGenerator,
        phraseGenerator: PhraseGenerator,
        shakeEvaluator: ShakeEvaluator,
        stepEvaluator: StepEvaluator
    ): MissionEngine {
        return MissionEngine(
            mathGenerator = mathGenerator,
            memoryGenerator = memoryGenerator,
            phraseGenerator = phraseGenerator,
            shakeEvaluator = shakeEvaluator,
            stepEvaluator = stepEvaluator
        )
    }

    /**
     * Provides the [AdManager] singleton.
     *
     * [AdManager] depends on [PremiumManager] to gate ad display for premium
     * users. We explicitly declare the provider here so the dependency is
     * visible in the DI graph and can be swapped in tests.
     *
     * NOTE: If you prefer to let Hilt auto-provision [AdManager] via its
     * `@Inject constructor`, you may safely remove this method.
     */
    @Provides
    @Singleton
    fun provideAdManager(
        premiumManager: PremiumManager
    ): AdManager {
        return AdManager(premiumManager)
    }
}
