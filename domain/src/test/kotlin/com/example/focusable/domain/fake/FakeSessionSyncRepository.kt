package com.example.focusable.domain.fake

import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.SessionSyncRepository
import com.example.focusable.domain.util.Resource

class FakeSessionSyncRepository : SessionSyncRepository {

    var shouldFail = false
    val syncedSessions = mutableListOf<FocusSession>()

    override suspend fun syncSession(session: FocusSession): Resource<Unit> {
        return if (shouldFail) {
            Resource.Error("Sync failed")
        } else {
            syncedSessions.add(session)
            Resource.Success(Unit)
        }
    }

    override suspend fun fetchSessions(): Resource<List<FocusSession>> {
        return if (shouldFail) {
            Resource.Error("Fetch failed")
        } else {
            Resource.Success(syncedSessions.toList())
        }
    }

    override suspend fun fetchSession(id: Long): Resource<FocusSession> {
        return if (shouldFail) {
            Resource.Error("Fetch failed")
        } else {
            val session = syncedSessions.firstOrNull { it.id == id }
            if (session != null) Resource.Success(session)
            else Resource.Error("Not found")
        }
    }
}
