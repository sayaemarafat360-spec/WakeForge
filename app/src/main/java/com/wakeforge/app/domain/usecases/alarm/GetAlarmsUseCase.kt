package com.wakeforge.app.domain.usecases.alarm

import com.wakeforge.app.domain.models.Alarm
import com.wakeforge.app.domain.repositories.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving all alarms sorted by their scheduled time.
 *
 * @property repository The [AlarmRepository] used for data retrieval.
 */
class GetAlarmsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {

    /**
     * Returns a reactive [Flow] of all alarms sorted by their next fire time.
     *
     * Alarms are sorted in ascending order (earliest first). Active alarms
     * appear before inactive ones at the same time.
     *
     * @return A [Flow] emitting the sorted list of [Alarm]s.
     */
    operator fun invoke(): Flow<List<Alarm>> {
        return repository.getAllAlarms().map { alarms ->
            alarms.sortedWith(
                compareBy<Alarm> { !it.isActive }
                    .thenBy { it.hour }
                    .thenBy { it.minute }
            )
        }
    }
}
