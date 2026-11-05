package com.protea.travels.data.local

import androidx.room.*

@Dao
interface CachedFlightDao {
    @Query("SELECT * FROM cached_flights WHERE routeKey = :routeKey ORDER BY price ASC")
    suspend fun forRoute(routeKey: String): List<CachedFlightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(flights: List<CachedFlightEntity>)

    @Query("DELETE FROM cached_flights WHERE routeKey = :routeKey")
    suspend fun clearRoute(routeKey: String)
}

@Dao
interface PendingBookingDao {
    @Insert
    suspend fun queue(booking: PendingBookingEntity): Long

    @Query("SELECT * FROM pending_bookings ORDER BY createdAt ASC")
    suspend fun all(): List<PendingBookingEntity>

    @Delete
    suspend fun remove(booking: PendingBookingEntity)

    @Query("SELECT COUNT(*) FROM pending_bookings")
    suspend fun count(): Int
}

@Dao
interface PendingAlertDao {
    @Insert
    suspend fun queue(alert: PendingAlertEntity): Long

    @Query("SELECT * FROM pending_alerts ORDER BY createdAt ASC")
    suspend fun all(): List<PendingAlertEntity>

    @Delete
    suspend fun remove(alert: PendingAlertEntity)
}
