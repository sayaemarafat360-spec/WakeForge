package com.wakeforge.app.domain.usecases.settings

import com.wakeforge.app.domain.models.AppSettings
import com.wakeforge.app.domain.repositories.SettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the current application settings.
 *
 * Provides reactive access to all user-configurable settings.
 *
 * @property repository The [SettingsRepository] used for settings retrieval.
 */
class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {

    /**
     * Returns a reactive [Flow] of the current [AppSettings].
     *
     * @return A [Flow] emitting the current [AppSettings] whenever any setting changes.
     */
    operator fun invoke(): Flow<AppSettings> {
        return repository.getSettings()
    }
}

