package com.example.focusable.data.repository

import com.example.focusable.data.remote.ApiService
import com.example.focusable.data.remote.mapper.SessionMapper
import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.repository.SessionSyncRepository
import com.example.focusable.domain.util.Resource

class SessionSyncRepositoryImpl(
    private val apiService: ApiService
) : SessionSyncRepository {

    override suspend fun syncSession(session: FocusSession): Resource<Unit> {
        return try {
            apiService.postSession(SessionMapper.domainToDto(session))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sync failed", e)
        }
    }

    override suspend fun fetchSessions(): Resource<List<FocusSession>> {
        return try {
            val sessions = apiService.getSessions().map(SessionMapper::dtoToDomain)
            Resource.Success(sessions)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Fetch failed", e)
        }
    }

    override suspend fun fetchSession(id: Long): Resource<FocusSession> {
        return try {
            val session = SessionMapper.dtoToDomain(apiService.getSession(id))
            Resource.Success(session)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Fetch failed", e)
        }
    }
}
