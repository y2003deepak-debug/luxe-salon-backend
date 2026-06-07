package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val phoneNumber: String,
    val clientName: String,
    val services: String,
    val stylistName: String,
    val date: String,
    val timeSlot: String,
    val status: String = "Active", // "Active" or "Cancelled"
    val priceEstimate: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "stylists")
data class Stylist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val specialty: String,
    val isAvailable: Boolean = true,
    val avatarColorIndex: Int = 0, // Used to style beautiful high-end gradient backgrounds in UI
    val imageUrl: String? = null,
    val awayUntilDate: String? = null,
    val awayUntilTime: String? = null
)

@Entity(tableName = "admin_profile")
data class AdminProfile(
    @PrimaryKey val id: Int = 1, // Row ID is locked at 1 for simplicity
    val email: String,
    val displayName: String,
    val customGreeting: String
)

@Entity(tableName = "salon_services")
data class SalonService(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    val description: String,
    val durationMin: Int = 30
)

