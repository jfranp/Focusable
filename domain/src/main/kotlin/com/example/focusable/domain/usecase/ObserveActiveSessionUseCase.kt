package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow

class ObserveActiveSessionUseCase(
    private val sessionRepository: FocusSessionRepository
) {
    operator fun invoke(): Flow<FocusSession?> {
        return sessionRepository.observeActiveSession()
    }
}
