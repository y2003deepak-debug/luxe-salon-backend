package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Booking::class, Stylist::class, AdminProfile::class, SalonService::class], version = 5, exportSchema = false)
abstract class SalonDatabase : RoomDatabase() {
    abstract fun bookingDao(): BookingDao
    abstract fun stylistDao(): StylistDao
    abstract fun adminProfileDao(): AdminProfileDao
    abstract fun salonServiceDao(): SalonServiceDao

    companion object {
        @Volatile
        private var INSTANCE: SalonDatabase? = null

        fun getDatabase(context: Context): SalonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalonDatabase::class.java,
                    "salon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
