package com.example.focusable.ui.screen.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.focusable.R
import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.ui.screen.home.levelDisplayName
import org.koin.androidx.compose.koinViewModel

@Composable
fun PreferencesScreen(
    viewModel: PreferencesViewModel = koinViewModel()
) {
    val sensorPrefs by viewModel.sensorPreferences.collectAsState()
    val isSessionActive by viewModel.isSessionActive.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.preferences_sensitivity_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.preferences_sensitivity_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isSessionActive) {
            Spacer(Modifier.height(12.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.preferences_active_session_warning),
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        SensorSection(
            title = stringResource(R.string.preferences_noise_sensitivity_title),
            currentLevel = sensorPrefs.noiseLevel,
            thresholdFormatter = { level ->
                val threshold = SensorPreferences(noiseLevel = level).noiseThreshold
                stringResource(R.string.preset_noise_threshold_format, threshold.toInt())
            },
            onSelect = { viewModel.selectNoiseLevel(it) }
        )

        Spacer(Modifier.height(24.dp))

        SensorSection(
            title = stringResource(R.string.preferences_motion_sensitivity_title),
            currentLevel = sensorPrefs.motionLevel,
            thresholdFormatter = { level ->
                val threshold = SensorPreferences(motionLevel = level).motionThreshold
                stringResource(R.string.preset_motion_threshold_format, "%.1f".format(threshold))
            },
            onSelect = { viewModel.selectMotionLevel(it) }
        )
    }
}

@Composable
private fun SensorSection(
    title: String,
    currentLevel: SensitivityLevel,
    thresholdFormatter: @Composable (SensitivityLevel) -> String,
    onSelect: (SensitivityLevel) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Medium
    )
    Spacer(Modifier.height(8.dp))

    SensitivityLevel.entries.forEach { level ->
        LevelOption(
            level = level,
            isSelected = currentLevel == level,
            thresholdLabel = thresholdFormatter(level),
            onSelect = { onSelect(level) }
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LevelOption(
    level: SensitivityLevel,
    isSelected: Boolean,
    thresholdLabel: String,
    onSelect: () -> Unit
) {
    val displayName = levelDisplayName(level)
    val description = levelDescription(level)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = thresholdLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun levelDescription(level: SensitivityLevel): String = when (level) {
    SensitivityLevel.NORMAL -> stringResource(R.string.preset_desc_normal)
    SensitivityLevel.SENSITIVE -> stringResource(R.string.preset_desc_sensitive)
    SensitivityLevel.EXTRA_SENSITIVE -> stringResource(R.string.preset_desc_extra_sensitive)
}
