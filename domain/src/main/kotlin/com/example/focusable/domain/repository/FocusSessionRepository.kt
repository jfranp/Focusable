package com.example.focusable.domain.repository

import com.example.focusable.domain.model.DistractionType
import com.example.focusable.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    fun observeActiveSession(): Flow<FocusSession?>
    fun observePastSessions(): Flow<List<FocusSession>>
    suspend fun startSession(): FocusSession
    suspend fun stopSession(): FocusSession?
    suspend fun getSession(id: Long): FocusSession?
    suspend fun recordDistraction(sessionId: Long, type: DistractionType)
}
