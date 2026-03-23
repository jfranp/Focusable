package com.example.focusable.data.remote

import com.example.focusable.data.remote.dto.SessionDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockRestInterceptorTest {

    private lateinit var client: OkHttpClient
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = "https://mock.api"

    @Before
    fun setUp() {
        client = OkHttpClient.Builder()
            .addInterceptor(MockRestInterceptor())
            .build()
    }

    @Test
    fun `POST sessions returns 201 with assigned id`() {
        val dto = SessionDto(startTime = 1000L, endTime = 2000L, totalDistractions = 3)
        val body = json.encodeToString(dto).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/sessions")
            .post(body)
            .build()

        val response = client.newCall(request).execute()

        assertEquals(201, response.code)
        val returned = json.decodeFromString<SessionDto>(response.body!!.string())
        assertNotNull(returned.id)
        assertTrue(returned.id!! > 0)
        assertEquals(1000L, returned.startTime)
        assertEquals(2000L, returned.endTime)
        assertEquals(3, returned.totalDistractions)
    }

    @Test
    fun `GET sessions returns empty list initially`() {
        val request = Request.Builder()
            .url("$baseUrl/sessions")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val list = json.decodeFromString<List<SessionDto>>(response.body!!.string())
        assertTrue(list.isEmpty())
    }

    @Test
    fun `GET sessions returns posted sessions`() {
        postSession(SessionDto(startTime = 1000L, endTime = 2000L, totalDistractions = 1))
        postSession(SessionDto(startTime = 3000L, endTime = 4000L, totalDistractions = 5))

        val request = Request.Builder()
            .url("$baseUrl/sessions")
            .get()
            .build()

        val response = client.newCall(request).execute()
        val list = json.decodeFromString<List<SessionDto>>(response.body!!.string())

        assertEquals(2, list.size)
    }

    @Test
    fun `GET session by id returns correct session`() {
        val posted = postSession(
            SessionDto(startTime = 1000L, endTime = 2000L, totalDistractions = 7)
        )

        val request = Request.Builder()
            .url("$baseUrl/session/${posted.id}")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        val returned = json.decodeFromString<SessionDto>(response.body!!.string())
        assertEquals(posted.id, returned.id)
        assertEquals(7, returned.totalDistractions)
    }

    @Test
    fun `GET session by unknown id returns 404`() {
        val request = Request.Builder()
            .url("$baseUrl/session/999")
            .get()
            .build()

        val response = client.newCall(request).execute()

        assertEquals(404, response.code)
    }

    @Test
    fun `POST assigns incrementing ids`() {
        val first = postSession(
            SessionDto(startTime = 1000L, endTime = 2000L, totalDistractions = 0)
        )
        val second = postSession(
            SessionDto(startTime = 3000L, endTime = 4000L, totalDistractions = 0)
        )

        assertTrue(second.id!! > first.id!!)
    }

    private fun postSession(dto: SessionDto): SessionDto {
        val body = json.encodeToString(dto).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/sessions")
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        return json.decodeFromString(response.body!!.string())
    }
}
