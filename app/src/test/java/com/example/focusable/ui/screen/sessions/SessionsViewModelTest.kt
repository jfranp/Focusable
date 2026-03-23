package com.example.focusable.ui.screen.sessions

import com.example.focusable.domain.usecase.ObservePastSessionsUseCase
import com.example.focusable.fake.FakeFocusSessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var vm: SessionsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionRepo = FakeFocusSessionRepository()
        vm = SessionsViewModel(
            observePastSessions = ObservePastSessionsUseCase(sessionRepo)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial sessions list is empty`() {
        assertTrue(vm.sessions.value.isEmpty())
    }

    @Test
    fun `sessions reflect past completed sessions`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.sessions.collect {} }

        sessionRepo.startSession()
        sessionRepo.stopSession()

        assertEquals(1, vm.sessions.value.size)
    }

    @Test
    fun `active session is not included in past sessions`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.sessions.collect {} }

        sessionRepo.startSession()

        assertTrue(vm.sessions.value.isEmpty())
    }

    @Test
    fun `multiple completed sessions appear in list`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.sessions.collect {} }

        sessionRepo.startSession()
        sessionRepo.stopSession()
        sessionRepo.startSession()
        sessionRepo.stopSession()

        assertEquals(2, vm.sessions.value.size)
    }
}
