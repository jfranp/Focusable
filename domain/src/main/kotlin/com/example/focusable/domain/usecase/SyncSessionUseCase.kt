package com.example.focusable.domain.usecase

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.SessionSyncRepository
import com.example.focusable.domain.util.Resource

class SyncSessionUseCase(
    private val syncRepository: SessionSyncRepository
) {
    suspend operator fun invoke(session: FocusSession): Resource<Unit> {
        return syncRepository.syncSession(session)
    }
}
