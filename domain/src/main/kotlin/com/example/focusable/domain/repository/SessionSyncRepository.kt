package com.example.focusable.domain.repository

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.util.Resource

interface SessionSyncRepository {
    suspend fun syncSession(session: FocusSession): Resource<Unit>
    suspend fun fetchSessions(): Resource<List<FocusSession>>
    suspend fun fetchSession(id: Long): Resource<FocusSession>
}
