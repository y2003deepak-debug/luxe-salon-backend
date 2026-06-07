package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

class MockServerInterceptor : Interceptor {

    // Central Simulated In-Memory Server Database
    companion object {
        var logs = mutableListOf<String>()

        var remoteServices = mutableListOf(
            SalonServiceDto(1, "Sculpted Cut", 120.0, "Elite cut & visual architecture consultation", 45),
            SalonServiceDto(2, "Artisan Color", 250.0, "Custom balayage coloring & gloss therapy", 120),
            SalonServiceDto(3, "Deep Hydration", 85.0, "Intense botanical scalp organic bath", 30),
            SalonServiceDto(4, "Signature Blowout", 75.0, "Silk infusion treatment extra volume blowout", 60)
        )

        var remoteStylists = mutableListOf(
            StylistDto(1, "Aarav Sharma", "Master Colorist", true, 0, null, null, null),
            StylistDto(2, "Priya Iyer", "Lead Hair Artisan", true, 1, null, null, null),
            StylistDto(3, "Amit Patel", "Creative Director", false, 2, null, null, null),
            StylistDto(4, "Rohan Das", "Treatments Lead", true, 3, null, null, null)
        )

        var remoteBookings = mutableListOf<BookingDto>()

        fun addLog(method: String, url: String, code: Int) {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            logs.add(0, "[$timestamp] $method $url -> $code")
            if (logs.size > 20) logs.removeAt(logs.size - 1)
        }
    }

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method
        val path = request.url.encodedPath

        // Log the active request
        var responseBodyString = ""
        var responseCode = 200

        try {
            when {
                // SERVICES ENDPOINTS
                path == "/api/services" && method == "GET" -> {
                    val adapter = moshi.adapter<List<SalonServiceDto>>(
                        Types.newParameterizedType(List::class.java, SalonServiceDto::class.java)
                    )
                    responseBodyString = adapter.toJson(remoteServices)
                    responseCode = 200
                }
                path == "/api/services" && method == "POST" -> {
                    val reqBody = request.body
                    var json = ""
                    if (reqBody != null) {
                        val buffer = okio.Buffer()
                        reqBody.writeTo(buffer)
                        json = buffer.readUtf8()
                    }
                    val itemAdapter = moshi.adapter(SalonServiceDto::class.java)
                    val newItem = itemAdapter.fromJson(json)
                    if (newItem != null) {
                        val maxId = remoteServices.maxOfOrNull { it.id } ?: 0
                        val savedItem = newItem.copy(id = maxId + 1)
                        remoteServices.add(savedItem)
                        responseBodyString = itemAdapter.toJson(savedItem)
                        responseCode = 201
                    } else {
                        responseBodyString = """{"error": "Invalid body"}"""
                        responseCode = 400
                    }
                }
                path.startsWith("/api/services/") && method == "DELETE" -> {
                    val idSegment = path.substringAfterLast("/")
                    val id = idSegment.toIntOrNull()
                    if (id != null) {
                        remoteServices.removeAll { it.id == id }
                        responseBodyString = "{}"
                        responseCode = 204
                    } else {
                        responseBodyString = """{"error": "Invalid ID"}"""
                        responseCode = 400
                    }
                }

                // STYLISTS ENDPOINTS
                path == "/api/stylists" && method == "GET" -> {
                    val adapter = moshi.adapter<List<StylistDto>>(
                        Types.newParameterizedType(List::class.java, StylistDto::class.java)
                    )
                    responseBodyString = adapter.toJson(remoteStylists)
                    responseCode = 200
                }
                path == "/api/stylists" && method == "POST" -> {
                    val reqBody = request.body
                    var json = ""
                    if (reqBody != null) {
                        val buffer = okio.Buffer()
                        reqBody.writeTo(buffer)
                        json = buffer.readUtf8()
                    }
                    val itemAdapter = moshi.adapter(StylistDto::class.java)
                    val newItem = itemAdapter.fromJson(json)
                    if (newItem != null) {
                        val maxId = remoteStylists.maxOfOrNull { it.id } ?: 0
                        val savedItem = newItem.copy(id = maxId + 1)
                        remoteStylists.add(savedItem)
                        responseBodyString = itemAdapter.toJson(savedItem)
                        responseCode = 201
                    } else {
                        responseBodyString = """{"error": "Invalid body"}"""
                        responseCode = 400
                    }
                }
                path.startsWith("/api/stylists/") && method == "DELETE" -> {
                    val idSegment = path.substringAfterLast("/")
                    val id = idSegment.toIntOrNull()
                    if (id != null) {
                        remoteStylists.removeAll { it.id == id }
                        responseBodyString = "{}"
                        responseCode = 204
                    } else {
                        responseBodyString = """{"error": "Invalid ID"}"""
                        responseCode = 400
                    }
                }

                // BOOKINGS ENDPOINTS
                path == "/api/bookings" && method == "GET" -> {
                    val adapter = moshi.adapter<List<BookingDto>>(
                        Types.newParameterizedType(List::class.java, BookingDto::class.java)
                    )
                    responseBodyString = adapter.toJson(remoteBookings)
                    responseCode = 200
                }
                path == "/api/bookings" && method == "POST" -> {
                    val reqBody = request.body
                    var json = ""
                    if (reqBody != null) {
                        val buffer = okio.Buffer()
                        reqBody.writeTo(buffer)
                        json = buffer.readUtf8()
                    }
                    val itemAdapter = moshi.adapter(BookingDto::class.java)
                    val newItem = itemAdapter.fromJson(json)
                    if (newItem != null) {
                        val maxId = remoteBookings.maxOfOrNull { it.id } ?: 0
                        val savedItem = newItem.copy(id = maxId + 1)
                        remoteBookings.add(savedItem)
                        responseBodyString = itemAdapter.toJson(savedItem)
                        responseCode = 201
                    } else {
                        responseBodyString = """{"error": "Invalid body"}"""
                        responseCode = 400
                    }
                }

                else -> {
                    responseBodyString = """{"error": "Not Found"}"""
                    responseCode = 404
                }
            }
        } catch (e: Exception) {
            responseBodyString = """{"error": "${e.message}"}"""
            responseCode = 500
        }

        addLog(method, path, responseCode)

        // Artificially simulate 150-250ms of network latency
        Thread.sleep(150 + (0..100).random().toLong())

        val mediaType = "application/json".toMediaTypeOrNull()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(responseCode)
            .message(if (responseCode == 204) "No Content" else "OK")
            .body(responseBodyString.toResponseBody(mediaType))
            .build()
    }
}
