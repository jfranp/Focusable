package com.example.focusable.ui.screen.home

import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.model.SensorTelemetrySnapshot
import com.example.focusable.domain.usecase.ObserveActiveSessionUseCase
import com.example.focusable.domain.usecase.ObserveSensorTelemetryUseCase
import com.example.focusable.domain.usecase.StartFocusSessionUseCase
import com.example.focusable.domain.usecase.StopFocusSessionUseCase
import com.example.focusable.domain.usecase.SyncSessionUseCase
import com.example.focusable.fake.FakeFocusSessionRepository
import com.example.focusable.fake.FakeSensorTelemetryPort
import com.example.focusable.fake.FakeSessionSyncRepository
import com.example.focusable.fake.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var prefsRepo: FakeUserPreferencesRepository
    private lateinit var syncRepo: FakeSessionSyncRepository
    private lateinit var telemetryPort: FakeSensorTelemetryPort
    private lateinit var vm: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionRepo = FakeFocusSessionRepository()
        prefsRepo = FakeUserPreferencesRepository()
        syncRepo = FakeSessionSyncRepository()
        telemetryPort = FakeSensorTelemetryPort()

        vm = HomeViewModel(
            observeActiveSession = ObserveActiveSessionUseCase(sessionRepo),
            observeTelemetry = ObserveSensorTelemetryUseCase(telemetryPort),
            startSessionUseCase = StartFocusSessionUseCase(sessionRepo),
            stopSessionUseCase = StopFocusSessionUseCase(sessionRepo),
            syncSessionUseCase = SyncSessionUseCase(syncRepo),
            preferencesRepository = prefsRepo
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has no active session`() {
        assertNull(vm.activeSession.value)
    }

    @Test
    fun `initial debug visibility is false`() {
        assertFalse(vm.isDebugVisible.value)
    }

    @Test
    fun `initial telemetry is default snapshot`() {
        assertEquals(SensorTelemetrySnapshot(), vm.telemetry.value)
    }

    @Test
    fun `initial sensor preferences are defaults`() {
        assertEquals(SensorPreferences(), vm.sensorPreferences.value)
    }

    @Test
    fun `toggleDebug flips visibility`() {
        assertFalse(vm.isDebugVisible.value)
        vm.toggleDebug()
        assertTrue(vm.isDebugVisible.value)
        vm.toggleDebug()
        assertFalse(vm.isDebugVisible.value)
    }

    @Test
    fun `onStartStopSession starts a session when none active`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.activeSession.collect {} }

        vm.onStartStopSession()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(vm.activeSession.value)
    }

    @Test
    fun `onStartStopSession stops and syncs an active session`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.activeSession.collect {} }

        vm.onStartStopSession()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(vm.activeSession.value)

        vm.onStartStopSession()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(vm.activeSession.value)
        assertEquals(1, syncRepo.syncedSessions.size)
    }

    @Test
    fun `telemetry updates reflect in state`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.telemetry.collect {} }

        val snapshot = SensorTelemetrySnapshot(
            noiseLevel = 5000f,
            motionLevel = 2.5f
        )
        telemetryPort.emit(snapshot)

        assertEquals(5000f, vm.telemetry.value.noiseLevel)
        assertEquals(2.5f, vm.telemetry.value.motionLevel)
    }
}
