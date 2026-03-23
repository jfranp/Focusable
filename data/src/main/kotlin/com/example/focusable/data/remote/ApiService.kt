package com.example.focusable.data.remote

import com.example.focusable.data.remote.dto.SessionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("sessions")
    suspend fun postSession(@Body session: SessionDto): SessionDto

    @GET("sessions")
    suspend fun getSessions(): List<SessionDto>

    @GET("session/{id}")
    suspend fun getSession(@Path("id") id: Long): SessionDto
}
