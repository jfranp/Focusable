package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.FocusSessionRepository

class StartFocusSessionUseCase(
    private val sessionRepository: FocusSessionRepository
) {
    suspend operator fun invoke(): FocusSession {
        return sessionRepository.startSession()
    }
}
