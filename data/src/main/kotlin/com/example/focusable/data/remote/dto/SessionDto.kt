package com.example.focusable.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SessionDto(
    val id: Long? = null,
    val startTime: Long,
    val endTime: Long,
    val totalDistractions: Int
)
