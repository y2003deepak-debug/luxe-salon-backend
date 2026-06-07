package com.example.data

import kotlinx.coroutines.flow.Flow

class SalonRepository(
    private val bookingDao: BookingDao,
    private val stylistDao: StylistDao,
    private val adminProfileDao: AdminProfileDao,
    private val salonServiceDao: SalonServiceDao
) {
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()
    val allStylists: Flow<List<Stylist>> = stylistDao.getAllStylists()
    val adminProfile: Flow<AdminProfile?> = adminProfileDao.getAdminProfile()
    val allServices: Flow<List<SalonService>> = salonServiceDao.getAllServices()

    fun getBookingsByPhone(phoneNumber: String): Flow<List<Booking>> {
        return bookingDao.getBookingsByPhone(phoneNumber)
    }

    suspend fun insertBooking(booking: Booking) {
        bookingDao.insertBooking(booking)
    }

    suspend fun updateBookingStatus(bookingId: Int, status: String) {
        bookingDao.updateBookingStatus(bookingId, status)
    }

    suspend fun deleteBooking(booking: Booking) {
        bookingDao.deleteBooking(booking)
    }

    suspend fun insertStylist(stylist: Stylist) {
        stylistDao.insertStylist(stylist)
    }

    suspend fun insertStylists(vararg stylists: Stylist) {
        stylistDao.insertStylists(*stylists)
    }

    suspend fun deleteStylist(stylist: Stylist) {
        stylistDao.deleteStylist(stylist)
    }

    suspend fun updateStylistAvailability(stylistId: Int, isAvailable: Boolean) {
        stylistDao.updateStylistAvailability(stylistId, isAvailable)
    }

    suspend fun updateStylistAvailabilityWithTime(stylistId: Int, isAvailable: Boolean, awayUntilDate: String?, awayUntilTime: String?) {
        stylistDao.updateStylistAvailabilityWithTime(stylistId, isAvailable, awayUntilDate, awayUntilTime)
    }

    suspend fun saveAdminProfile(profile: AdminProfile) {
        adminProfileDao.saveAdminProfile(profile)
    }

    suspend fun insertService(service: SalonService) {
        salonServiceDao.insertService(service)
    }

    suspend fun insertServices(vararg services: SalonService) {
        salonServiceDao.insertServices(*services)
    }

    suspend fun deleteService(service: SalonService) {
        salonServiceDao.deleteService(service)
    }

    suspend fun updateService(service: SalonService) {
        salonServiceDao.updateService(service)
    }

    suspend fun clearAllData() {
        bookingDao.deleteAllBookings()
        stylistDao.deleteAllStylists()
        salonServiceDao.deleteAllServices()
    }
}
