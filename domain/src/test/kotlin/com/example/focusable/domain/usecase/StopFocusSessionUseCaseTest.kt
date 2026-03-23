package com.example.focusable.domain.usecase

import com.example.focusable.domain.fake.FakeFocusSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class StopFocusSessionUseCaseTest {

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var useCase: StopFocusSessionUseCase

    @Before
    fun setUp() {
        sessionRepo = FakeFocusSessionRepository()
        useCase = StopFocusSessionUseCase(sessionRepo)
    }

    @Test
    fun `invoke returns stopped session with endTime when active session exists`() = runTest {
        sessionRepo.startSession()

        val stopped = useCase()

        assertNotNull(stopped)
        assertNotNull(stopped!!.endTime)
    }

    @Test
    fun `invoke returns null when no active session`() = runTest {
        val stopped = useCase()

        assertNull(stopped)
    }

    @Test
    fun `invoke preserves session startTime after stopping`() = runTest {
        val started = sessionRepo.startSession()

        val stopped = useCase()

        assertNotNull(stopped)
        assert(stopped!!.startTime == started.startTime)
    }
}
