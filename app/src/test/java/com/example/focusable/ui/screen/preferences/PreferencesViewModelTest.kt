package com.example.focusable.ui.screen.preferences

import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.usecase.ObserveActiveSessionUseCase
import com.example.focusable.domain.usecase.UpdateSensorPreferenceUseCase
import com.example.focusable.fake.FakeFocusSessionRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var prefsRepo: FakeUserPreferencesRepository
    private lateinit var vm: PreferencesViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        sessionRepo = FakeFocusSessionRepository()
        prefsRepo = FakeUserPreferencesRepository()
        vm = PreferencesViewModel(
            preferencesRepository = prefsRepo,
            updatePreferenceUseCase = UpdateSensorPreferenceUseCase(prefsRepo, sessionRepo),
            observeActiveSession = ObserveActiveSessionUseCase(sessionRepo)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial preferences are defaults`() {
        assertEquals(SensorPreferences(), vm.sensorPreferences.value)
    }

    @Test
    fun `initial isSessionActive is false`() {
        assertFalse(vm.isSessionActive.value)
    }

    @Test
    fun `isSessionActive becomes true when session is started`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.isSessionActive.collect {} }

        sessionRepo.startSession()

        assertTrue(vm.isSessionActive.value)
    }

    @Test
    fun `selectNoiseLevel updates noise preference`() = runTest {
        vm.selectNoiseLevel(SensitivityLevel.SENSITIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SensitivityLevel.SENSITIVE, prefsRepo.currentPreferences.noiseLevel)
    }

    @Test
    fun `selectMotionLevel updates motion preference`() = runTest {
        vm.selectMotionLevel(SensitivityLevel.EXTRA_SENSITIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(SensitivityLevel.EXTRA_SENSITIVE, prefsRepo.currentPreferences.motionLevel)
    }

    @Test
    fun `selectNoiseLevel stops active session`() = runTest {
        backgroundScope.launch(testDispatcher) { vm.isSessionActive.collect {} }

        sessionRepo.startSession()
        assertTrue(vm.isSessionActive.value)

        vm.selectNoiseLevel(SensitivityLevel.SENSITIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(vm.isSessionActive.value)
    }

    @Test
    fun `noise and motion levels are independent`() = runTest {
        vm.selectNoiseLevel(SensitivityLevel.EXTRA_SENSITIVE)
        vm.selectMotionLevel(SensitivityLevel.SENSITIVE)
        testDispatcher.scheduler.advanceUntilIdle()

        val prefs = prefsRepo.currentPreferences
        assertEquals(SensitivityLevel.EXTRA_SENSITIVE, prefs.noiseLevel)
        assertEquals(SensitivityLevel.SENSITIVE, prefs.motionLevel)
    }
}
