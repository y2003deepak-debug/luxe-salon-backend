package com.example.data.api

import com.example.data.Booking
import com.example.data.SalonService
import com.example.data.Stylist
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// Data Transfer Objects (DTOs) for Client-Server synchronization
data class SalonServiceDto(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val durationMin: Int,
    val nameHindi: String = "",
    val suitability: String = "",
    val isPremium: Boolean = false
) {
    fun toRoomEntity() = SalonService(
        id = id,
        name = name,
        price = price,
        description = description,
        durationMin = durationMin,
        nameHindi = nameHindi,
        suitability = suitability,
        isPremium = isPremium
    )

    companion object {
        fun fromRoomEntity(entity: SalonService) = SalonServiceDto(
            id = entity.id,
            name = entity.name,
            price = entity.price,
            description = entity.description,
            durationMin = entity.durationMin,
            nameHindi = entity.nameHindi,
            suitability = entity.suitability,
            isPremium = entity.isPremium
        )
    }
}

data class StylistDto(
    val id: Int,
    val name: String,
    val specialty: String,
    val isAvailable: Boolean,
    val avatarColorIndex: Int,
    val imageUrl: String?,
    val awayUntilDate: String?,
    val awayUntilTime: String?
) {
    fun toRoomEntity() = Stylist(
        id = id,
        name = name,
        specialty = specialty,
        isAvailable = isAvailable,
        avatarColorIndex = avatarColorIndex,
        imageUrl = imageUrl,
        awayUntilDate = awayUntilDate,
        awayUntilTime = awayUntilTime
    )

    companion object {
        fun fromRoomEntity(entity: Stylist) = StylistDto(
            id = entity.id,
            name = entity.name,
            specialty = entity.specialty,
            isAvailable = entity.isAvailable,
            avatarColorIndex = entity.avatarColorIndex,
            imageUrl = entity.imageUrl,
            awayUntilDate = entity.awayUntilDate,
            awayUntilTime = entity.awayUntilTime
        )
    }
}

data class BookingDto(
    val id: Int,
    val phoneNumber: String,
    val clientName: String,
    val services: String,
    val stylistName: String,
    val date: String,
    val timeSlot: String,
    val status: String,
    val priceEstimate: Double,
    val timestamp: Long
) {
    fun toRoomEntity() = Booking(
        id = id,
        phoneNumber = phoneNumber,
        clientName = clientName,
        services = services,
        stylistName = stylistName,
        date = date,
        timeSlot = timeSlot,
        status = status,
        priceEstimate = priceEstimate,
        timestamp = timestamp
    )

    companion object {
        fun fromRoomEntity(entity: Booking) = BookingDto(
            id = entity.id,
            phoneNumber = entity.phoneNumber,
            clientName = entity.clientName,
            services = entity.services,
            stylistName = entity.stylistName,
            date = entity.date,
            timeSlot = entity.timeSlot,
            status = entity.status,
            priceEstimate = entity.priceEstimate,
            timestamp = entity.timestamp
        )
    }
}

// Retrofit REST API Contract
interface SalonApiService {
    @GET("api/services")
    suspend fun getServices(): List<SalonServiceDto>

    @POST("api/services")
    suspend fun createService(@Body service: SalonServiceDto): SalonServiceDto

    @DELETE("api/services/{id}")
    suspend fun deleteService(@Path("id") id: Int): Response<Unit>

    @GET("api/stylists")
    suspend fun getStylists(): List<StylistDto>

    @POST("api/stylists")
    suspend fun createStylist(@Body stylist: StylistDto): StylistDto

    @DELETE("api/stylists/{id}")
    suspend fun deleteStylist(@Path("id") id: Int): Response<Unit>

    @GET("api/bookings")
    suspend fun getBookings(): List<BookingDto>

    @POST("api/bookings")
    suspend fun createBooking(@Body booking: BookingDto): BookingDto

    @DELETE("api/bookings/{id}")
    suspend fun deleteBooking(@Path("id") id: Int): Response<Unit>

    @PUT("api/bookings/{id}/status")
    suspend fun updateBookingStatus(@Path("id") id: Int, @Body statusUpdate: StatusUpdateDto): BookingDto
}

data class StatusUpdateDto(
    val status: String
)

// API engine initialization
object RetrofitClient {
    private const val BASE_URL = "https://luxe-salon-backend-ekvo.onrender.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Explicit client with mock interceptor injected during viewModel setup
    var clientFactory: (OkHttpClient.Builder.() -> Unit) -> SalonApiService = { configurator ->
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .also { configurator(it) }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        retrofit.create(SalonApiService::class.java)
    }
}
