package com.example.focusable.domain.model

data class DistractionEvent(
    val id: Long = 0,
    val sessionId: Long,
    val type: DistractionType,
    val timestamp: Long
)
