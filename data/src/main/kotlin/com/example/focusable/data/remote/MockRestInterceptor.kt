package com.example.focusable.data.remote

import com.example.focusable.data.remote.dto.SessionDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class MockRestInterceptor : Interceptor {

    private val json = Json { ignoreUnknownKeys = true }
    private val sessions = ConcurrentHashMap<Long, SessionDto>()
    private val idCounter = AtomicLong(1)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method

        return when {
            method == "POST" && path.endsWith("/sessions") ->
                handlePostSession(request)

            method == "GET" && path.endsWith("/sessions") && !path.contains("/session/") ->
                handleGetSessions(request)

            method == "GET" && Regex(".*/session/\\d+$").matches(path) -> {
                val id = path.substringAfterLast("/").toLong()
                handleGetSession(request, id)
            }

            else -> chain.proceed(request)
        }
    }

    private fun handlePostSession(request: okhttp3.Request): Response {
        val bodyString = request.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        } ?: return errorResponse(request, 400, "Missing body")

        val dto = json.decodeFromString<SessionDto>(bodyString)
        val id = idCounter.getAndIncrement()
        val stored = dto.copy(id = id)
        sessions[id] = stored

        return jsonResponse(request, 201, json.encodeToString(stored))
    }

    private fun handleGetSessions(request: okhttp3.Request): Response {
        val list = sessions.values.toList()
        return jsonResponse(request, 200, json.encodeToString(list))
    }

    private fun handleGetSession(request: okhttp3.Request, id: Long): Response {
        val session = sessions[id]
            ?: return errorResponse(request, 404, "Session not found")
        return jsonResponse(request, 200, json.encodeToString(session))
    }

    private fun jsonResponse(request: okhttp3.Request, code: Int, body: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(httpMessage(code))
            .body(body.toResponseBody(jsonMediaType))
            .build()
    }

    private fun errorResponse(request: okhttp3.Request, code: Int, message: String): Response {
        val errorBody = json.encodeToString(mapOf("error" to message))
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(message)
            .body(errorBody.toResponseBody(jsonMediaType))
            .build()
    }

    private fun httpMessage(code: Int): String = when (code) {
        200 -> "OK"
        201 -> "Created"
        400 -> "Bad Request"
        404 -> "Not Found"
        else -> "Error"
    }
}
