package com.example.focusable.domain.usecase

import com.example.focusable.domain.fake.FakeFocusSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartFocusSessionUseCaseTest {

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var useCase: StartFocusSessionUseCase

    @Before
    fun setUp() {
        sessionRepo = FakeFocusSessionRepository()
        useCase = StartFocusSessionUseCase(sessionRepo)
    }

    @Test
    fun `invoke returns session with startTime set`() = runTest {
        val session = useCase()

        assertTrue(session.startTime > 0)
    }

    @Test
    fun `invoke returns session with no endTime`() = runTest {
        val session = useCase()

        assertNull(session.endTime)
    }

    @Test
    fun `invoke returns session with assigned id`() = runTest {
        val session = useCase()

        assertTrue(session.id > 0)
    }

    @Test
    fun `invoke returns session with zero distractions`() = runTest {
        val session = useCase()

        assertTrue(session.totalDistractions == 0)
    }
}
