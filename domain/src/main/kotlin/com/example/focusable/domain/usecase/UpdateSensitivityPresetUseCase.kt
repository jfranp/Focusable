package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.repository.FocusSessionRepository
import com.example.focusable.domain.repository.UserPreferencesRepository

class UpdateSensorPreferenceUseCase(
    private val preferencesRepository: UserPreferencesRepository,
    private val sessionRepository: FocusSessionRepository
) {
    suspend fun updateNoiseLevel(level: SensitivityLevel) {
        sessionRepository.stopSession()
        preferencesRepository.updateNoiseLevel(level)
    }

    suspend fun updateMotionLevel(level: SensitivityLevel) {
        sessionRepository.stopSession()
        preferencesRepository.updateMotionLevel(level)
    }
}
