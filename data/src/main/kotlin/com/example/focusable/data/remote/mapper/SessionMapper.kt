package com.example.focusable.data.remote.mapper

import com.example.focusable.data.local.db.FocusSessionEntity
import com.example.focusable.data.remote.dto.SessionDto
import com.example.focusable.domain.model.FocusSession

object SessionMapper {

    fun entityToDomain(entity: FocusSessionEntity): FocusSession {
        return FocusSession(
            id = entity.id,
            startTime = entity.startTime,
            endTime = entity.endTime,
            totalDistractions = entity.totalDistractions,
            isSynced = entity.isSynced
        )
    }

    fun domainToEntity(session: FocusSession): FocusSessionEntity {
        return FocusSessionEntity(
            id = session.id,
            startTime = session.startTime,
            endTime = session.endTime,
            totalDistractions = session.totalDistractions,
            isSynced = session.isSynced
        )
    }

    fun domainToDto(session: FocusSession): SessionDto {
        return SessionDto(
            id = if (session.id == 0L) null else session.id,
            startTime = session.startTime,
            endTime = session.endTime ?: session.startTime,
            totalDistractions = session.totalDistractions
        )
    }

    fun dtoToDomain(dto: SessionDto): FocusSession {
        return FocusSession(
            id = dto.id ?: 0,
            startTime = dto.startTime,
            endTime = dto.endTime,
            totalDistractions = dto.totalDistractions,
            isSynced = true
        )
    }
}
