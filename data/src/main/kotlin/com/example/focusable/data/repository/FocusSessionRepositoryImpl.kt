package com.example.focusable.data.repository

import com.example.focusable.data.local.db.FocusSessionDao
import com.example.focusable.data.local.db.FocusSessionEntity
import com.example.focusable.data.remote.mapper.SessionMapper
import com.example.focusable.data.sensor.FocusSessionController
import com.example.focusable.domain.model.DistractionType
import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.FocusSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FocusSessionRepositoryImpl(
    private val dao: FocusSessionDao,
    private val controller: FocusSessionController
) : FocusSessionRepository {

    override fun observeActiveSession(): Flow<FocusSession?> {
        return dao.observeActiveSession().map { it?.let(SessionMapper::entityToDomain) }
    }

    override fun observePastSessions(): Flow<List<FocusSession>> {
        return dao.observePastSessions().map { list ->
            list.map(SessionMapper::entityToDomain)
        }
    }

    override suspend fun startSession(): FocusSession {
        stopSession()

        val entity = FocusSessionEntity(startTime = System.currentTimeMillis())
        val id = dao.insert(entity)
        val session = entity.copy(id = id)

        controller.startMonitoring(id)

        return SessionMapper.entityToDomain(session)
    }

    override suspend fun stopSession(): FocusSession? {
        val active = dao.getActiveSession() ?: return null
        val updated = active.copy(endTime = System.currentTimeMillis())
        dao.update(updated)

        controller.stopMonitoring()

        return SessionMapper.entityToDomain(updated)
    }

    override suspend fun getSession(id: Long): FocusSession? {
        return dao.getById(id)?.let(SessionMapper::entityToDomain)
    }

    override suspend fun recordDistraction(sessionId: Long, type: DistractionType) {
        val session = dao.getById(sessionId) ?: return
        dao.update(session.copy(totalDistractions = session.totalDistractions + 1))
    }
}
