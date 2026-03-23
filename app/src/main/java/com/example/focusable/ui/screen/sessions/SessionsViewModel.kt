package com.example.focusable.ui.screen.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.focusable.domain.usecase.ObservePastSessionsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SessionsViewModel(
    observePastSessions: ObservePastSessionsUseCase
) : ViewModel() {

    val sessions = observePastSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
