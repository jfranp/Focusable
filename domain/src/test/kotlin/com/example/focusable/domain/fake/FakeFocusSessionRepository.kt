package com.example.focusable.domain.fake

import com.example.focusable.domain.model.DistractionType
import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFocusSessionRepository : FocusSessionRepository {

    private val sessions = MutableStateFlow<List<FocusSession>>(emptyList())
    private var nextId = 1L

    val stopSessionCallCount get() = _stopSessionCallCount
    private var _stopSessionCallCount = 0

    override fun observeActiveSession(): Flow<FocusSession?> =
        sessions.map { list -> list.firstOrNull { it.endTime == null } }

    override fun observePastSessions(): Flow<List<FocusSession>> =
        sessions.map { list -> list.filter { it.endTime != null } }

    override suspend fun startSession(): FocusSession {
        val session = FocusSession(
            id = nextId++,
            startTime = System.currentTimeMillis()
        )
        sessions.update { it + session }
        return session
    }

    override suspend fun stopSession(): FocusSession? {
        _stopSessionCallCount++
        val active = sessions.value.firstOrNull { it.endTime == null } ?: return null
        val stopped = active.copy(endTime = System.currentTimeMillis())
        sessions.update { list ->
            list.map { if (it.id == active.id) stopped else it }
        }
        return stopped
    }

    override suspend fun getSession(id: Long): FocusSession? =
        sessions.value.firstOrNull { it.id == id }

    override suspend fun recordDistraction(sessionId: Long, type: DistractionType) {
        sessions.update { list ->
            list.map {
                if (it.id == sessionId) it.copy(totalDistractions = it.totalDistractions + 1)
                else it
            }
        }
    }
}
