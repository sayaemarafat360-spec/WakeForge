package com.wakeforge.app.domain.repositories

import com.wakeforge.app.domain.models.Alarm
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for alarm CRUD operations and scheduling.
 */
interface AlarmRepository {

    /**
     * Observes all alarms as a reactive [Flow].
     *
     * @return A [Flow] emitting the full list of [Alarm]s whenever the data changes.
     */
    fun getAllAlarms(): Flow<List<Alarm>>

    /**
     * Observes a single alarm by its ID.
     *
     * @param id The unique identifier of the alarm.
     * @return A [Flow] emitting the [Alarm] or null if not found.
     */
    fun getAlarmById(id: String): Flow<Alarm?>

    /**
     * Observes the next upcoming active alarm.
     *
     * @return A [Flow] emitting the nearest [Alarm] or null if none are active.
     */
    fun getNextActiveAlarm(): Flow<Alarm?>

    /**
     * Persists a new alarm to the data store and schedules it.
     *
     * @param alarm The [Alarm] to create.
     */
    suspend fun createAlarm(alarm: Alarm)

    /**
     * Updates an existing alarm in the data store.
     *
     * @param alarm The [Alarm] with updated fields.
     */
    suspend fun updateAlarm(alarm: Alarm)

    /**
     * Deletes an alarm by its ID and cancels its scheduled trigger.
     *
     * @param id The unique identifier of the alarm to delete.
     */
    suspend fun deleteAlarm(id: String)

    /**
     * Toggles the active state of an alarm.
     *
     * When activating, the alarm is scheduled. When deactivating, it is cancelled.
     *
     * @param id The unique identifier of the alarm.
     * @param isActive The new active state.
     */
    suspend fun toggleAlarm(id: String, isActive: Boolean)

    /**
     * Schedules an alarm with the Android alarm manager.
     *
     * @param alarm The [Alarm] to schedule.
     */
    suspend fun scheduleAlarm(alarm: Alarm)

    /**
     * Cancels a previously scheduled alarm.
     *
     * @param alarmId The unique identifier of the alarm to cancel.
     */
    suspend fun cancelAlarm(alarmId: String)

    /**
     * Cancels all scheduled alarms. Typically used when the app is uninstalled
     * or when a full reset is performed.
     */
    suspend fun cancelAllAlarms()
}
