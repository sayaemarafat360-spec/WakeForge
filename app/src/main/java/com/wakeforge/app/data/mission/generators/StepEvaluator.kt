package com.wakeforge.app.data.mission.generators

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.wakeforge.app.data.mission.DifficultyConfigurator
import com.wakeforge.app.domain.models.Mission
import com.wakeforge.app.domain.models.MissionDifficulty
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StepEvaluator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurator: DifficultyConfigurator
) {

    companion object {
        private const val TAG = "StepEvaluator"
    }

    private val sensorManager: SensorManager?
        get() = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var initialStepCount: Int = -1

    fun createStepMission(difficulty: MissionDifficulty): Mission.StepMission {
        val requiredSteps = configurator.getStepCount(difficulty)
        val timeLimitMs = configurator.getStepTimeLimit(difficulty)

        return Mission.StepMission(
            id = UUID.randomUUID().toString(),
            type = com.wakeforge.app.domain.models.MissionType.STEP,
            difficulty = difficulty,
            isTimed = true,
            timeLimitMs = timeLimitMs,
            requiredSteps = requiredSteps,
            currentSteps = 0
        )
    }

    fun startListening(onStep: () -> Unit): SensorEventListener? {
        val sm = sensorManager ?: return null
        val stepSensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return null

        resetInitialCount()

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                    val currentSteps = event.values[0].toInt()

                    if (initialStepCount < 0) {
                        initialStepCount = currentSteps
                    }

                    if (currentSteps > initialStepCount) {
                        onStep()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sm.registerListener(
            listener,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )

        return listener
    }

    fun stopListening(listener: SensorEventListener) {
        sensorManager?.unregisterListener(listener)
        resetInitialCount()
    }

    fun isSensorAvailable(): Boolean {
        val sm = sensorManager ?: return false
        return sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    fun resetInitialCount() {
        initialStepCount = -1
    }
}
