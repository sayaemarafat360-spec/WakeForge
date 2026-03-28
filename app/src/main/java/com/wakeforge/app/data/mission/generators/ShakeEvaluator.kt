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
class ShakeEvaluator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val configurator: DifficultyConfigurator
) {

    companion object {
        private const val SHAKE_THRESHOLD = 12f
        private const val SHAKE_COOLDOWN_MS = 500L
    }

    private val sensorManager: SensorManager?
        get() = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var lastShakeTime = 0L

    fun createShakeMission(difficulty: MissionDifficulty): Mission.ShakeMission {
        val requiredShakes = configurator.getShakeCount(difficulty)
        val timeLimitMs = configurator.getShakeTimeLimit(difficulty)

        return Mission.ShakeMission(
            id = UUID.randomUUID().toString(),
            type = com.wakeforge.app.domain.models.MissionType.SHAKE,
            difficulty = difficulty,
            isTimed = true,
            timeLimitMs = timeLimitMs,
            requiredShakes = requiredShakes,
            currentShakes = 0,
            shakeThreshold = SHAKE_THRESHOLD
        )
    }

    fun startListening(onShake: () -> Unit): SensorEventListener? {
        val sm = sensorManager ?: return null
        val accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (isSignificantShake(event)) {
                    onShake()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        sm.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        return listener
    }

    fun stopListening(listener: SensorEventListener) {
        sensorManager?.unregisterListener(listener)
    }

    private fun isSignificantShake(event: SensorEvent): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastShakeTime < SHAKE_COOLDOWN_MS) {
            return false
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val accelerationX = x - SensorManager.GRAVITY_EARTH
        val accelerationY = y - SensorManager.GRAVITY_EARTH
        val accelerationZ = z - SensorManager.GRAVITY_EARTH

        val magnitude = kotlin.math.sqrt(
            accelerationX * accelerationX +
                accelerationY * accelerationY +
                accelerationZ * accelerationZ
        )

        if (magnitude > SHAKE_THRESHOLD) {
            lastShakeTime = currentTime
            return true
        }

        return false
    }
}
