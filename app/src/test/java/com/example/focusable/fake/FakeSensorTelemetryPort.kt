package com.example.focusable.fake

import com.example.focusable.domain.model.SensorTelemetrySnapshot
import com.example.focusable.domain.sensor.SensorTelemetryPort
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSensorTelemetryPort : SensorTelemetryPort {

    private val _telemetry = MutableStateFlow(SensorTelemetrySnapshot())

    fun emit(snapshot: SensorTelemetrySnapshot) {
        _telemetry.value = snapshot
    }

    override fun observeTelemetry(): Flow<SensorTelemetrySnapshot> =
        _telemetry.asStateFlow()
}
