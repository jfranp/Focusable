package com.example.focusable.domain.model

data class SensorTelemetrySnapshot(
    val noiseLevel: Float = 0f,
    val motionLevel: Float = 0f,
    val noiseThreshold: Float = 0f,
    val motionThreshold: Float = 0f,
    val isSampling: Boolean = false
)
