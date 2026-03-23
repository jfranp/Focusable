package com.example.focusable.domain.model

enum class SensitivityLevel {
    NORMAL, SENSITIVE, EXTRA_SENSITIVE
}

data class SensorPreferences(
    val noiseLevel: SensitivityLevel = SensitivityLevel.NORMAL,
    val motionLevel: SensitivityLevel = SensitivityLevel.NORMAL
) {
    val noiseThreshold: Float get() = when (noiseLevel) {
        SensitivityLevel.NORMAL -> 3000f
        SensitivityLevel.SENSITIVE -> 1800f
        SensitivityLevel.EXTRA_SENSITIVE -> 1000f
    }

    val motionThreshold: Float get() = when (motionLevel) {
        SensitivityLevel.NORMAL -> 3.0f
        SensitivityLevel.SENSITIVE -> 1.8f
        SensitivityLevel.EXTRA_SENSITIVE -> 1.0f
    }
}
