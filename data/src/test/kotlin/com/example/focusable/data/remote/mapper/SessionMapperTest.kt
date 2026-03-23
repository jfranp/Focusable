package com.example.focusable.data.remote.mapper

import com.example.focusable.data.local.db.FocusSessionEntity
import com.example.focusable.data.remote.dto.SessionDto
import com.example.focusable.domain.model.FocusSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionMapperTest {

    @Test
    fun `entityToDomain maps all fields correctly`() {
        val entity = FocusSessionEntity(
            id = 5,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 3,
            isSynced = true
        )

        val domain = SessionMapper.entityToDomain(entity)

        assertEquals(5L, domain.id)
        assertEquals(1000L, domain.startTime)
        assertEquals(2000L, domain.endTime)
        assertEquals(3, domain.totalDistractions)
        assertTrue(domain.isSynced)
    }

    @Test
    fun `entityToDomain preserves null endTime`() {
        val entity = FocusSessionEntity(
            id = 1,
            startTime = 1000L,
            endTime = null,
            totalDistractions = 0
        )

        val domain = SessionMapper.entityToDomain(entity)

        assertNull(domain.endTime)
    }

    @Test
    fun `domainToEntity maps all fields correctly`() {
        val domain = FocusSession(
            id = 7,
            startTime = 3000L,
            endTime = 4000L,
            totalDistractions = 2,
            isSynced = false
        )

        val entity = SessionMapper.domainToEntity(domain)

        assertEquals(7L, entity.id)
        assertEquals(3000L, entity.startTime)
        assertEquals(4000L, entity.endTime)
        assertEquals(2, entity.totalDistractions)
        assertEquals(false, entity.isSynced)
    }

    @Test
    fun `domainToDto sets id to null when id is zero`() {
        val domain = FocusSession(
            id = 0,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 1
        )

        val dto = SessionMapper.domainToDto(domain)

        assertNull(dto.id)
    }

    @Test
    fun `domainToDto preserves non-zero id`() {
        val domain = FocusSession(
            id = 10,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 1
        )

        val dto = SessionMapper.domainToDto(domain)

        assertEquals(10L, dto.id)
    }

    @Test
    fun `domainToDto uses startTime as endTime fallback when endTime is null`() {
        val domain = FocusSession(
            id = 1,
            startTime = 5000L,
            endTime = null,
            totalDistractions = 0
        )

        val dto = SessionMapper.domainToDto(domain)

        assertEquals(5000L, dto.endTime)
    }

    @Test
    fun `dtoToDomain marks session as synced`() {
        val dto = SessionDto(
            id = 1,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 4
        )

        val domain = SessionMapper.dtoToDomain(dto)

        assertTrue(domain.isSynced)
    }

    @Test
    fun `dtoToDomain defaults id to zero when dto id is null`() {
        val dto = SessionDto(
            id = null,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 0
        )

        val domain = SessionMapper.dtoToDomain(dto)

        assertEquals(0L, domain.id)
    }

    @Test
    fun `dtoToDomain maps all fields correctly`() {
        val dto = SessionDto(
            id = 3,
            startTime = 1000L,
            endTime = 2000L,
            totalDistractions = 5
        )

        val domain = SessionMapper.dtoToDomain(dto)

        assertEquals(3L, domain.id)
        assertEquals(1000L, domain.startTime)
        assertEquals(2000L, domain.endTime)
        assertEquals(5, domain.totalDistractions)
    }
}
