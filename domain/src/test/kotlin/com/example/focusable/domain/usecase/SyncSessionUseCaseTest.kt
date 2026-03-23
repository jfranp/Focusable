package com.example.focusable.domain.usecase

import com.example.focusable.domain.fake.FakeSessionSyncRepository
import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.util.Resource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SyncSessionUseCaseTest {

    private lateinit var syncRepo: FakeSessionSyncRepository
    private lateinit var useCase: SyncSessionUseCase

    private val testSession = FocusSession(
        id = 1,
        startTime = 1000L,
        endTime = 2000L,
        totalDistractions = 3
    )

    @Before
    fun setUp() {
        syncRepo = FakeSessionSyncRepository()
        useCase = SyncSessionUseCase(syncRepo)
    }

    @Test
    fun `invoke returns Success on successful sync`() = runTest {
        val result = useCase(testSession)

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `invoke stores session in sync repository on success`() = runTest {
        useCase(testSession)

        assertEquals(1, syncRepo.syncedSessions.size)
        assertEquals(testSession, syncRepo.syncedSessions.first())
    }

    @Test
    fun `invoke returns Error when sync fails`() = runTest {
        syncRepo.shouldFail = true

        val result = useCase(testSession)

        assertTrue(result is Resource.Error)
    }

    @Test
    fun `invoke does not store session when sync fails`() = runTest {
        syncRepo.shouldFail = true

        useCase(testSession)

        assertTrue(syncRepo.syncedSessions.isEmpty())
    }
}
