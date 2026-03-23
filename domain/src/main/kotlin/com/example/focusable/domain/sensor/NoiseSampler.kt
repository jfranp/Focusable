package com.example.focusable.domain.sensor

import kotlinx.coroutines.flow.Flow

interface NoiseSampler {
    val noiseLevel: Flow<Float>
    fun start()
    fun stop()
}
