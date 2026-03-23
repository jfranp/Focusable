package com.example.focusable.domain.sensor

import com.example.focusable.domain.model.SensorTelemetrySnapshot
import kotlinx.coroutines.flow.Flow

interface SensorTelemetryPort {
    fun observeTelemetry(): Flow<SensorTelemetrySnapshot>
}
