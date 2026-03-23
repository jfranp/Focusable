package com.example.focusable.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SensorPreferencesTest {

    @Test
    fun `default preferences use NORMAL for both sensors`() {
        val prefs = SensorPreferences()
        assertEquals(SensitivityLevel.NORMAL, prefs.noiseLevel)
        assertEquals(SensitivityLevel.NORMAL, prefs.motionLevel)
    }

    @Test
    fun `NORMAL noise threshold is 3000`() {
        val prefs = SensorPreferences(noiseLevel = SensitivityLevel.NORMAL)
        assertEquals(3000f, prefs.noiseThreshold, 0.01f)
    }

    @Test
    fun `SENSITIVE noise threshold is 1800`() {
        val prefs = SensorPreferences(noiseLevel = SensitivityLevel.SENSITIVE)
        assertEquals(1800f, prefs.noiseThreshold, 0.01f)
    }

    @Test
    fun `EXTRA_SENSITIVE noise threshold is 1000`() {
        val prefs = SensorPreferences(noiseLevel = SensitivityLevel.EXTRA_SENSITIVE)
        assertEquals(1000f, prefs.noiseThreshold, 0.01f)
    }

    @Test
    fun `NORMAL motion threshold is 3_0`() {
        val prefs = SensorPreferences(motionLevel = SensitivityLevel.NORMAL)
        assertEquals(3.0f, prefs.motionThreshold, 0.01f)
    }

    @Test
    fun `SENSITIVE motion threshold is 1_8`() {
        val prefs = SensorPreferences(motionLevel = SensitivityLevel.SENSITIVE)
        assertEquals(1.8f, prefs.motionThreshold, 0.01f)
    }

    @Test
    fun `EXTRA_SENSITIVE motion threshold is 1_0`() {
        val prefs = SensorPreferences(motionLevel = SensitivityLevel.EXTRA_SENSITIVE)
        assertEquals(1.0f, prefs.motionThreshold, 0.01f)
    }

    @Test
    fun `noise and motion levels are independent`() {
        val prefs = SensorPreferences(
            noiseLevel = SensitivityLevel.EXTRA_SENSITIVE,
            motionLevel = SensitivityLevel.NORMAL
        )
        assertEquals(1000f, prefs.noiseThreshold, 0.01f)
        assertEquals(3.0f, prefs.motionThreshold, 0.01f)
    }
}
