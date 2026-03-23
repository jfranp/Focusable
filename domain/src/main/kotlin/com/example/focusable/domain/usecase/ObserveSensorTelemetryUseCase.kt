package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.SensorTelemetrySnapshot
import com.example.focusable.domain.sensor.SensorTelemetryPort
import kotlinx.coroutines.flow.Flow

class ObserveSensorTelemetryUseCase(
    private val telemetryPort: SensorTelemetryPort
) {
    operator fun invoke(): Flow<SensorTelemetrySnapshot> {
        return telemetryPort.observeTelemetry()
    }
}
