package com.example.focusable.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.model.SensorTelemetrySnapshot
import com.example.focusable.domain.repository.UserPreferencesRepository
import com.example.focusable.domain.usecase.ObserveActiveSessionUseCase
import com.example.focusable.domain.usecase.ObserveSensorTelemetryUseCase
import com.example.focusable.domain.usecase.StartFocusSessionUseCase
import com.example.focusable.domain.usecase.StopFocusSessionUseCase
import com.example.focusable.domain.usecase.SyncSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeActiveSession: ObserveActiveSessionUseCase,
    observeTelemetry: ObserveSensorTelemetryUseCase,
    private val startSessionUseCase: StartFocusSessionUseCase,
    private val stopSessionUseCase: StopFocusSessionUseCase,
    private val syncSessionUseCase: SyncSessionUseCase,
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _isDebugVisible = MutableStateFlow(false)
    val isDebugVisible = _isDebugVisible.asStateFlow()

    val activeSession = observeActiveSession()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val telemetry = observeTelemetry()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SensorTelemetrySnapshot())

    val sensorPreferences = preferencesRepository.observeSensorPreferences()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SensorPreferences())

    fun toggleDebug() {
        _isDebugVisible.update { !it }
    }

    fun onStartStopSession() {
        viewModelScope.launch {
            val current = activeSession.value
            if (current != null) {
                val stopped = stopSessionUseCase()
                if (stopped != null) {
                    syncSessionUseCase(stopped)
                }
            } else {
                startSessionUseCase()
            }
        }
    }
}
