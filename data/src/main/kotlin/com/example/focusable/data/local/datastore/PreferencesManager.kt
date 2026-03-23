package com.example.focusable.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "focusable_preferences")

class PreferencesManager(private val context: Context) {

    private val noiseLevelKey = stringPreferencesKey("noise_sensitivity")
    private val motionLevelKey = stringPreferencesKey("motion_sensitivity")

    val sensorPreferencesFlow: Flow<SensorPreferences> = context.dataStore.data.map { prefs ->
        val noiseLevel = parseSensitivityLevel(prefs[noiseLevelKey])
        val motionLevel = parseSensitivityLevel(prefs[motionLevelKey])
        SensorPreferences(noiseLevel = noiseLevel, motionLevel = motionLevel)
    }

    suspend fun updateNoiseLevel(level: SensitivityLevel) {
        context.dataStore.edit { prefs ->
            prefs[noiseLevelKey] = level.name
        }
    }

    suspend fun updateMotionLevel(level: SensitivityLevel) {
        context.dataStore.edit { prefs ->
            prefs[motionLevelKey] = level.name
        }
    }

    private fun parseSensitivityLevel(name: String?): SensitivityLevel {
        if (name == null) return SensitivityLevel.NORMAL
        return try {
            SensitivityLevel.valueOf(name)
        } catch (_: IllegalArgumentException) {
            SensitivityLevel.NORMAL
        }
    }
}
