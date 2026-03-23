package com.example.focusable.domain.sensor

import kotlinx.coroutines.flow.Flow

interface MotionSampler {
    val motionLevel: Flow<Float>
    fun start()
    fun stop()
}
