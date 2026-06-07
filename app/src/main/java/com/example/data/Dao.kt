package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY timestamp DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE phoneNumber = :phone ORDER BY timestamp DESC")
    fun getBookingsByPhone(phone: String): Flow<List<Booking>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking)

    @Update
    suspend fun updateBooking(booking: Booking)

    @Delete
    suspend fun deleteBooking(booking: Booking)

    @Query("DELETE FROM bookings")
    suspend fun deleteAllBookings()

    @Query("UPDATE bookings SET status = :status WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Int, status: String)
}

@Dao
interface StylistDao {
    @Query("SELECT * FROM stylists ORDER BY id ASC")
    fun getAllStylists(): Flow<List<Stylist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylist(stylist: Stylist)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStylists(vararg stylists: Stylist)

    @Delete
    suspend fun deleteStylist(stylist: Stylist)

    @Query("DELETE FROM stylists")
    suspend fun deleteAllStylists()

    @Query("UPDATE stylists SET isAvailable = :isAvailable WHERE id = :stylistId")
    suspend fun updateStylistAvailability(stylistId: Int, isAvailable: Boolean)

    @Query("UPDATE stylists SET isAvailable = :isAvailable, awayUntilDate = :awayUntilDate, awayUntilTime = :awayUntilTime WHERE id = :stylistId")
    suspend fun updateStylistAvailabilityWithTime(stylistId: Int, isAvailable: Boolean, awayUntilDate: String?, awayUntilTime: String?)
}

@Dao
interface AdminProfileDao {
    @Query("SELECT * FROM admin_profile WHERE id = 1 LIMIT 1")
    fun getAdminProfile(): Flow<AdminProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdminProfile(profile: AdminProfile)
}

@Dao
interface SalonServiceDao {
    @Query("SELECT * FROM salon_services ORDER BY id ASC")
    fun getAllServices(): Flow<List<SalonService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: SalonService)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(vararg services: SalonService)

    @Delete
    suspend fun deleteService(service: SalonService)

    @Query("DELETE FROM salon_services")
    suspend fun deleteAllServices()

    @Update
    suspend fun updateService(service: SalonService)
}

