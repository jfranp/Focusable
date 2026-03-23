package com.example.focusable.domain.usecase

import com.example.focusable.domain.fake.FakeFocusSessionRepository
import com.example.focusable.domain.fake.FakeUserPreferencesRepository
import com.example.focusable.domain.model.SensitivityLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateSensorPreferenceUseCaseTest {

    private lateinit var sessionRepo: FakeFocusSessionRepository
    private lateinit var prefsRepo: FakeUserPreferencesRepository
    private lateinit var useCase: UpdateSensorPreferenceUseCase

    @Before
    fun setUp() {
        sessionRepo = FakeFocusSessionRepository()
        prefsRepo = FakeUserPreferencesRepository()
        useCase = UpdateSensorPreferenceUseCase(prefsRepo, sessionRepo)
    }

    @Test
    fun `updateNoiseLevel stops active session`() = runTest {
        sessionRepo.startSession()

        useCase.updateNoiseLevel(SensitivityLevel.SENSITIVE)

        assertEquals(1, sessionRepo.stopSessionCallCount)
    }

    @Test
    fun `updateNoiseLevel updates noise level in preferences`() = runTest {
        useCase.updateNoiseLevel(SensitivityLevel.EXTRA_SENSITIVE)

        assertEquals(SensitivityLevel.EXTRA_SENSITIVE, prefsRepo.currentPreferences.noiseLevel)
    }

    @Test
    fun `updateNoiseLevel does not affect motion level`() = runTest {
        prefsRepo.updateMotionLevel(SensitivityLevel.SENSITIVE)

        useCase.updateNoiseLevel(SensitivityLevel.EXTRA_SENSITIVE)

        assertEquals(SensitivityLevel.SENSITIVE, prefsRepo.currentPreferences.motionLevel)
    }

    @Test
    fun `updateMotionLevel stops active session`() = runTest {
        sessionRepo.startSession()

        useCase.updateMotionLevel(SensitivityLevel.SENSITIVE)

        assertEquals(1, sessionRepo.stopSessionCallCount)
    }

    @Test
    fun `updateMotionLevel updates motion level in preferences`() = runTest {
        useCase.updateMotionLevel(SensitivityLevel.EXTRA_SENSITIVE)

        assertEquals(SensitivityLevel.EXTRA_SENSITIVE, prefsRepo.currentPreferences.motionLevel)
    }

    @Test
    fun `updateMotionLevel does not affect noise level`() = runTest {
        prefsRepo.updateNoiseLevel(SensitivityLevel.SENSITIVE)

        useCase.updateMotionLevel(SensitivityLevel.EXTRA_SENSITIVE)

        assertEquals(SensitivityLevel.SENSITIVE, prefsRepo.currentPreferences.noiseLevel)
    }
}
