package com.example.focusable.ui.screen.home

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.focusable.R
import com.example.focusable.domain.model.FocusSession
import com.example.focusable.domain.model.SensitivityLevel
import com.example.focusable.domain.model.SensorPreferences
import com.example.focusable.domain.model.SensorTelemetrySnapshot
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    isDebugVisible: Boolean
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val sensorPrefs by viewModel.sensorPreferences.collectAsState()
    val isActive = activeSession != null

    KeepScreenOn(isActive)

    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    val startTime = activeSession?.startTime

    LaunchedEffect(startTime) {
        if (startTime != null) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        } else {
            elapsedSeconds = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            val debugAlpha by animateFloatAsState(
                targetValue = if (isDebugVisible) 1f else 0f,
                animationSpec = tween(300),
                label = "debugAlpha"
            )
            DebugDashboard(
                telemetry = telemetry,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .alpha(debugAlpha)
            )
        }

        SessionButton(
            isActive = isActive,
            onClick = { viewModel.onStartStopSession() }
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            SessionInfoPanel(
                session = activeSession,
                elapsedSeconds = elapsedSeconds,
                sensorPreferences = sensorPrefs,
                isActive = isActive,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
private fun SessionButton(isActive: Boolean, onClick: () -> Unit) {
    val containerColor = if (isActive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }
    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onError
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        modifier = Modifier.size(200.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = if (isActive)
                stringResource(R.string.home_stop_session)
            else
                stringResource(R.string.home_start_session),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SessionInfoPanel(
    session: FocusSession?,
    elapsedSeconds: Long,
    sensorPreferences: SensorPreferences,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isActive) {
                Text(
                    text = formatDuration(elapsedSeconds),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.home_distractions_format,
                        session?.totalDistractions ?: 0
                    ),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.home_noise_level_format,
                        levelDisplayName(sensorPreferences.noiseLevel)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.home_motion_level_format,
                        levelDisplayName(sensorPreferences.motionLevel)
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = stringResource(R.string.home_no_active_session),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_tap_to_start),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DebugDashboard(
    telemetry: SensorTelemetrySnapshot,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_debug_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            if (!telemetry.isSampling) {
                Text(
                    text = stringResource(R.string.home_debug_not_sampling),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                DebugMetricRow(
                    label = stringResource(R.string.home_debug_noise_level),
                    value = telemetry.noiseLevel,
                    threshold = telemetry.noiseThreshold
                )
                Spacer(Modifier.height(8.dp))
                DebugMetricRow(
                    label = stringResource(R.string.home_debug_motion_level),
                    value = telemetry.motionLevel,
                    threshold = telemetry.motionThreshold
                )
            }
        }
    }
}

@Composable
private fun DebugMetricRow(label: String, value: Float, threshold: Float) {
    val isAbove = value > threshold
    val valueColor = if (isAbove) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "%.1f / %.1f".format(value, threshold),
                color = valueColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (isAbove) {
                Text(
                    text = stringResource(R.string.home_debug_above_threshold),
                    color = valueColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun KeepScreenOn(enabled: Boolean) {
    val context = LocalContext.current
    val window = (context as? Activity)?.window

    DisposableEffect(enabled) {
        if (enabled) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

@Composable
fun levelDisplayName(level: SensitivityLevel): String = when (level) {
    SensitivityLevel.NORMAL -> stringResource(R.string.preset_name_normal)
    SensitivityLevel.SENSITIVE -> stringResource(R.string.preset_name_sensitive)
    SensitivityLevel.EXTRA_SENSITIVE -> stringResource(R.string.preset_name_extra_sensitive)
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
