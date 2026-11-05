package com.protea.travels.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CachedFlightEntity::class, PendingBookingEntity::class, PendingAlertEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cachedFlights(): CachedFlightDao
    abstract fun pendingBookings(): PendingBookingDao
    abstract fun pendingAlerts(): PendingAlertDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "protea_offline.db"
            ).build().also { instance = it }
        }
    }
}
