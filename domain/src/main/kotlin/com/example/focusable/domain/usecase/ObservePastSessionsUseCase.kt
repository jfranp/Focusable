package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow

class ObservePastSessionsUseCase(
    private val sessionRepository: FocusSessionRepository
) {
    operator fun invoke(): Flow<List<FocusSession>> {
        return sessionRepository.observePastSessions()
    }
}
