package com.protea.travels.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Offline mode with sync (Room).
 *
 * CachedFlightEntity: the last successful search result for a route is written here
 * after every online search, so the same route can still be displayed -- read-only --
 * while the device has no connection.
 *
 * PendingBookingEntity / PendingAlertEntity: an action the user takes while offline
 * (booking a flight, adding a price alert) is written here instead of being lost,
 * and is pushed to Firestore by SyncManager the moment connectivity returns.
 */
@Entity(tableName = "cached_flights")
data class CachedFlightEntity(
    @PrimaryKey val id: String,
    val routeKey: String, // "JNB-CPT"
    val from: String, val to: String, val airline: String,
    val departTime: String, val arriveTime: String,
    val durationMin: Int, val price: Int,
    val cachedAt: Long
)

@Entity(tableName = "pending_bookings")
data class PendingBookingEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val from: String, val to: String, val airline: String,
    val departTime: String, val departDate: String, val returnDate: String,
    val passengers: Int, val cabin: String, val tripType: String,
    val total: Int, val createdAt: Long
)

@Entity(tableName = "pending_alerts")
data class PendingAlertEntity(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val from: String, val to: String, val createdAt: Long
)
