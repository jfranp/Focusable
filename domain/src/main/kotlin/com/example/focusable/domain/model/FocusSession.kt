package com.example.focusable.domain.model

data class FocusSession(
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val totalDistractions: Int = 0,
    val isSynced: Boolean = false
)
