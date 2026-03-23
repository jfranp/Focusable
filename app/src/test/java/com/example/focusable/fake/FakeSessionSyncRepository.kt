package com.example.focusable.fake

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.SessionSyncRepository
import com.example.focusable.domain.util.Resource

class FakeSessionSyncRepository : SessionSyncRepository {

    val syncedSessions = mutableListOf<FocusSession>()

    override suspend fun syncSession(session: FocusSession): Resource<Unit> {
        syncedSessions.add(session)
        return Resource.Success(Unit)
    }

    override suspend fun fetchSessions(): Resource<List<FocusSession>> =
        Resource.Success(syncedSessions.toList())

    override suspend fun fetchSession(id: Long): Resource<FocusSession> {
        val session = syncedSessions.firstOrNull { it.id == id }
        return if (session != null) Resource.Success(session)
        else Resource.Error("Not found")
    }
}
