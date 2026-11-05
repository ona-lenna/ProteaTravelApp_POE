package com.protea.travels

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.protea.travels.data.local.AppDatabase
import com.protea.travels.data.local.CachedFlightEntity
import com.protea.travels.data.local.PendingAlertEntity
import com.protea.travels.data.local.PendingBookingEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test: needs an emulator or phone, because Room uses Android's SQLite.
 * Uses an in-memory database, so it never touches the real offline cache.
 */
@RunWith(AndroidJUnit4::class)
class RoomOfflineTest {
    private lateinit var db: AppDatabase

    @Before fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After fun closeDb() = db.close()

    private fun flight(id: String, route: String, price: Int) =
        CachedFlightEntity(id, route, route.substringBefore('-'), route.substringAfter('-'),
            "Protea Air", "06:30", "08:35", 125, price, 0L)

    @Test fun cachedFlightsComeBackSortedByPriceForTheirRouteOnly() = runBlocking {
        val dao = db.cachedFlights()
        dao.upsertAll(listOf(flight("a", "JNB-CPT", 3000), flight("b", "JNB-CPT", 2450), flight("c", "JNB-DUR", 1890)))
        val result = dao.forRoute("JNB-CPT")
        assertEquals(listOf("b", "a"), result.map { it.id })
    }

    @Test fun clearingARouteLeavesOtherRoutesAlone() = runBlocking {
        val dao = db.cachedFlights()
        dao.upsertAll(listOf(flight("a", "JNB-CPT", 2450), flight("c", "JNB-DUR", 1890)))
        dao.clearRoute("JNB-CPT")
        assertTrue(dao.forRoute("JNB-CPT").isEmpty())
        assertEquals(1, dao.forRoute("JNB-DUR").size)
    }

    @Test fun upsertingTheSameFlightTwiceKeepsOneRow() = runBlocking {
        val dao = db.cachedFlights()
        dao.upsertAll(listOf(flight("a", "JNB-CPT", 2450)))
        dao.upsertAll(listOf(flight("a", "JNB-CPT", 2100)))
        assertEquals(listOf(2100), dao.forRoute("JNB-CPT").map { it.price })
    }

    @Test fun bookingsMadeOfflineAreQueuedCountedAndRemovedOnceSynced() = runBlocking {
        val dao = db.pendingBookings()
        assertEquals(0, dao.count())
        dao.queue(PendingBookingEntity(0, "JNB", "CPT", "Protea Air", "06:30", "2026-09-15", "2026-09-22", 2, "Economy", "Return", 9800, 1L))
        dao.queue(PendingBookingEntity(0, "JNB", "DUR", "Karoo Airways", "18:45", "2026-10-03", "", 1, "Business", "One-way", 4725, 2L))
        assertEquals(2, dao.count())
        val queued = dao.all()
        assertEquals(listOf("CPT", "DUR"), queued.map { it.to }) // oldest first
        dao.remove(queued.first())
        assertEquals(1, dao.count())
    }

    @Test fun alertsMadeOfflineAreQueuedAndRemovedOnceSynced() = runBlocking {
        val dao = db.pendingAlerts()
        dao.queue(PendingAlertEntity(0, "JNB", "LHR", 1L))
        val queued = dao.all()
        assertEquals(1, queued.size)
        dao.remove(queued.first())
        assertTrue(dao.all().isEmpty())
    }
}
