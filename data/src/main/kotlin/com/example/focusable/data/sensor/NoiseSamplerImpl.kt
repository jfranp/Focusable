package com.example.focusable.data.sensor

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.focusable.domain.sensor.NoiseSampler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class NoiseSamplerImpl(private val context: Context) : NoiseSampler {

    private val _noiseLevel = MutableStateFlow(0f)
    override val noiseLevel: Flow<Float> = _noiseLevel.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var samplingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @SuppressLint("MissingPermission")
    override fun start() {
        if (samplingJob?.isActive == true) return

        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (bufferSize == AudioRecord.ERROR_BAD_VALUE || bufferSize == AudioRecord.ERROR) return

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            return
        }

        audioRecord?.startRecording()
        val buffer = ShortArray(bufferSize / 2)

        samplingJob = scope.launch {
            while (isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    var sumSquares = 0.0
                    for (i in 0 until read) {
                        val sample = buffer[i].toDouble()
                        sumSquares += sample * sample
                    }
                    _noiseLevel.value = sqrt(sumSquares / read).toFloat()
                }
                delay(200)
            }
        }
    }

    override fun stop() {
        samplingJob?.cancel()
        samplingJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: IllegalStateException) { }
        audioRecord = null
        _noiseLevel.value = 0f
    }
}
