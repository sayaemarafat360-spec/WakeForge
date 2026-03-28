package com.wakeforge.app.di

import android.app.Application
import android.content.Context
import com.wakeforge.app.core.utils.NotificationUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// ──────────────────────────────────────────────────────────────────────────────
// Qualifiers
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Qualifier for the application-scoped [CoroutineScope] so that it can be
 * distinguished from ViewModel or lifecycle-scoped coroutines.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/**
 * Qualifier for the main (UI) [CoroutineScope].
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainCoroutineScope

/**
 * Qualifier for the I/O-bound [CoroutineScope].
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoCoroutineScope

// ──────────────────────────────────────────────────────────────────────────────
// Module
// ──────────────────────────────────────────────────────────────────────────────

/**
 * Hilt module that provides application-wide singletons such as the
 * [Context], coroutine scopes, and notification utilities.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the application [Context] via the [ApplicationContext] qualifier.
     * This is the canonical way to inject context throughout the app.
     */
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    /**
     * Application-scoped coroutine scope backed by [SupervisorJob] so a child
     * failure does not cancel sibling coroutines.
     *
     * Uses [Dispatchers.Default] which is optimised for CPU-bound work.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    /**
     * I/O-optimised coroutine scope for database, datastore, and network
     * operations.
     */
    @Provides
    @Singleton
    @IoCoroutineScope
    fun provideIoCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    /**
     * Provides [NotificationUtils] as a singleton. The backing object is
     * stateless, but exposing it through Hilt allows easy replacement in
     * tests and keeps the DI graph explicit.
     */
    @Provides
    @Singleton
    fun provideNotificationUtils(): NotificationUtils {
        return NotificationUtils
    }
}
