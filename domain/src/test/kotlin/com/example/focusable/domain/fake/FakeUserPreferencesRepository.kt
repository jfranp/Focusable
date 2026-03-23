package com.example.focusable.domain.fake

import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeUserPreferencesRepository : UserPreferencesRepository {

    private val _preferences = MutableStateFlow(SensorPreferences())

    val currentPreferences: SensorPreferences get() = _preferences.value

    override fun observeSensorPreferences(): Flow<SensorPreferences> =
        _preferences.asStateFlow()

    override suspend fun updateNoiseLevel(level: SensitivityLevel) {
        _preferences.update { it.copy(noiseLevel = level) }
    }

    override suspend fun updateMotionLevel(level: SensitivityLevel) {
        _preferences.update { it.copy(motionLevel = level) }
    }
}
