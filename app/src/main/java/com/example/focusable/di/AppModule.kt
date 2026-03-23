package com.example.focusable.di

import com.example.focusable.domain.usecase.ObserveActiveSessionUseCase
import com.example.focusable.domain.usecase.ObservePastSessionsUseCase
import com.example.focusable.domain.usecase.ObserveSensorTelemetryUseCase
import com.example.focusable.domain.usecase.StartFocusSessionUseCase
import com.example.focusable.domain.usecase.StopFocusSessionUseCase
import com.example.focusable.domain.usecase.SyncSessionUseCase
import com.example.focusable.domain.usecase.UpdateSensorPreferenceUseCase
import com.example.focusable.ui.screen.home.HomeViewModel
import com.example.focusable.ui.screen.preferences.PreferencesViewModel
import com.example.focusable.ui.screen.sessions.SessionsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    factory { StartFocusSessionUseCase(get()) }
    factory { StopFocusSessionUseCase(get()) }
    factory { ObserveActiveSessionUseCase(get()) }
    factory { ObservePastSessionsUseCase(get()) }
    factory { UpdateSensorPreferenceUseCase(get(), get()) }
    factory { SyncSessionUseCase(get()) }
    factory { ObserveSensorTelemetryUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SessionsViewModel(get()) }
    viewModel { PreferencesViewModel(get(), get(), get()) }
}
