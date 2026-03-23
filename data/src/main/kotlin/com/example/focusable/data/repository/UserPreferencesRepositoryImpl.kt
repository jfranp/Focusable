package com.example.focusable.data.repository

import com.example.focusable.data.local.datastore.PreferencesManager
import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow

class UserPreferencesRepositoryImpl(
    private val preferencesManager: PreferencesManager
) : UserPreferencesRepository {

    override fun observeSensorPreferences(): Flow<SensorPreferences> =
        preferencesManager.sensorPreferencesFlow

    override suspend fun updateNoiseLevel(level: SensitivityLevel) =
        preferencesManager.updateNoiseLevel(level)

    override suspend fun updateMotionLevel(level: SensitivityLevel) =
        preferencesManager.updateMotionLevel(level)
}
