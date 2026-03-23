package com.example.focusable.data.sensor

import com.example.focusable.data.local.datastore.PreferencesManager
import com.example.focusable.data.local.db.FocusSessionDao
import com.example.focusable.data.notification.NotificationHelper
import com.example.focusable.domain.model.DistractionType
import com.example.focusable.domain.model.SensorTelemetrySnapshot
import com.example.focusable.domain.sensor.MotionSampler
import com.example.focusable.domain.sensor.NoiseSampler
import com.example.focusable.domain.sensor.SensorTelemetryPort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusSessionController(
    private val noiseSampler: NoiseSampler,
    private val motionSampler: MotionSampler,
    private val notificationHelper: NotificationHelper,
    private val sessionDao: FocusSessionDao,
    private val preferencesManager: PreferencesManager
) : SensorTelemetryPort {

    private val _telemetry = MutableStateFlow(SensorTelemetrySnapshot())

    override fun observeTelemetry(): Flow<SensorTelemetrySnapshot> = _telemetry.asStateFlow()

    private var monitoringScope: CoroutineScope? = null
    private var activeSessionId: Long? = null

    @Volatile
    private var lastNoiseNotificationTime = 0L

    @Volatile
    private var lastMotionNotificationTime = 0L

    companion object {
        private const val NOISE_THROTTLE_MS = 5_000L
        private const val MOTION_THROTTLE_MS = 3_000L
    }

    fun startMonitoring(sessionId: Long) {
        stopMonitoring()
        activeSessionId = sessionId
        lastNoiseNotificationTime = 0L
        lastMotionNotificationTime = 0L

        noiseSampler.start()
        motionSampler.start()

        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        monitoringScope = scope

        scope.launch {
            combine(noiseSampler.noiseLevel, motionSampler.motionLevel) { noise, motion ->
                noise to motion
            }.collect { (noise, motion) ->
                val sensorPrefs = preferencesManager.sensorPreferencesFlow.first()

                _telemetry.value = SensorTelemetrySnapshot(
                    noiseLevel = noise,
                    motionLevel = motion,
                    noiseThreshold = sensorPrefs.noiseThreshold,
                    motionThreshold = sensorPrefs.motionThreshold,
                    isSampling = true
                )

                val now = System.currentTimeMillis()

                if (noise > sensorPrefs.noiseThreshold &&
                    now - lastNoiseNotificationTime > NOISE_THROTTLE_MS
                ) {
                    lastNoiseNotificationTime = now
                    handleDistraction(DistractionType.NOISE)
                }

                if (motion > sensorPrefs.motionThreshold &&
                    now - lastMotionNotificationTime > MOTION_THROTTLE_MS
                ) {
                    lastMotionNotificationTime = now
                    handleDistraction(DistractionType.MOTION)
                }
            }
        }
    }

    fun stopMonitoring() {
        monitoringScope?.cancel()
        monitoringScope = null
        noiseSampler.stop()
        motionSampler.stop()
        activeSessionId = null
        _telemetry.value = SensorTelemetrySnapshot()
    }

    private suspend fun handleDistraction(type: DistractionType) {
        val session = sessionDao.getActiveSession() ?: return
        sessionDao.update(session.copy(totalDistractions = session.totalDistractions + 1))
        notificationHelper.showDistractionNotification(type)
    }
}
