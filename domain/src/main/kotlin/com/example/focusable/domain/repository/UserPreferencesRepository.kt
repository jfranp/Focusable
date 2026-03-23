package com.example.focusable.domain.repository

import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun observeSensorPreferences(): Flow<SensorPreferences>
    suspend fun updateNoiseLevel(level: SensitivityLevel)
    suspend fun updateMotionLevel(level: SensitivityLevel)
}
