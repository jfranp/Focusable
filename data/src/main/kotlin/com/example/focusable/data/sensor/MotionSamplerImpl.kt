package com.example.focusable.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.focusable.domain.sensor.MotionSampler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class MotionSamplerImpl(private val context: Context) : MotionSampler {

    private val _motionLevel = MutableStateFlow(0f)
    override val motionLevel: Flow<Float> = _motionLevel.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var listener: SensorEventListener? = null

    override fun start() {
        if (sensorManager != null) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return

        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                _motionLevel.value = abs(magnitude - SensorManager.GRAVITY_EARTH)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager?.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun stop() {
        listener?.let { sensorManager?.unregisterListener(it) }
        sensorManager = null
        listener = null
        _motionLevel.value = 0f
    }
}
