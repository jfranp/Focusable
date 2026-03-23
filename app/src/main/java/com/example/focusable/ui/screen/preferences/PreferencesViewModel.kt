package com.example.focusable.ui.screen.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.repository.UserPreferencesRepository
import com.example.focusable.domain.usecase.ObserveActiveSessionUseCase
import com.example.focusable.domain.usecase.UpdateSensorPreferenceUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(
    preferencesRepository: UserPreferencesRepository,
    private val updatePreferenceUseCase: UpdateSensorPreferenceUseCase,
    observeActiveSession: ObserveActiveSessionUseCase
) : ViewModel() {

    val sensorPreferences = preferencesRepository.observeSensorPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SensorPreferences())

    val isSessionActive = observeActiveSession()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun selectNoiseLevel(level: SensitivityLevel) {
        viewModelScope.launch {
            updatePreferenceUseCase.updateNoiseLevel(level)
        }
    }

    fun selectMotionLevel(level: SensitivityLevel) {
        viewModelScope.launch {
            updatePreferenceUseCase.updateMotionLevel(level)
        }
    }
}
