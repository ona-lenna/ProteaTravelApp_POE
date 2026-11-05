package com.protea.travels.data

import android.content.Context
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.protea.travels.data.local.AppDatabase
import com.protea.travels.data.local.CachedFlightEntity
import com.protea.travels.data.local.PendingAlertEntity
import com.protea.travels.data.local.PendingBookingEntity
import com.protea.travels.data.local.isOnline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * All online calls: Firebase Auth (passwords hashed server-side, plus Google and
 * Facebook single sign-on) + Cloud Firestore (database) + Cloud Messaging (push).
 * Offline reads/writes fall back to the local Room cache in data/local and are
 * reconciled by syncPending() once connectivity returns -- see AppVM.
 */
object Repo {
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Set once from MainActivity/ProteaApp so Repo can reach Room and connectivity
    // without every screen having to pass a Context down through AppVM.
    private var appContext: Context? = null
    fun init(context: Context) { appContext = context.applicationContext }
    private fun requireContext() = appContext
        ?: error("Repo.init(context) must be called once before Repo is used")
    private fun database() = AppDatabase.get(requireContext())

    val isLoggedIn get() = auth.currentUser != null
    val email get() = auth.currentUser?.email ?: ""
    private val uid get() = auth.currentUser!!.uid
    private fun userDoc() = db.collection("users").document(uid)

    private val defaultProfileFields = mapOf(
        "darkMode" to false, "currency" to "ZAR", "notifications" to true,
        "defaultOrigin" to "JNB", "language" to "en"
    )

    // ---------- Email/password ----------
    suspend fun register(first: String, last: String, email: String, pw: String) {
        auth.createUserWithEmailAndPassword(email, pw).await()
        userDoc().set(defaultProfileFields + mapOf("firstName" to first, "lastName" to last, "email" to email)).await()
    }

    suspend fun login(email: String, pw: String) { auth.signInWithEmailAndPassword(email, pw).await() }
    suspend fun resetPassword(email: String) { auth.sendPasswordResetEmail(email).await() }
    fun logout() = auth.signOut()

    // ---------- Single sign-on (Google / Facebook) ----------
    /** idToken comes from the Google Sign-In client (see AuthScreens.kt / MainActivity). */
    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        ensureProfileExists(result.user?.displayName)
    }

    /** accessToken comes from the Facebook LoginManager callback. */
    suspend fun signInWithFacebook(accessToken: String) {
        val credential = FacebookAuthProvider.getCredential(accessToken)
        val result = auth.signInWithCredential(credential).await()
        ensureProfileExists(result.user?.displayName)
    }

    /** A social sign-in may be a brand-new account, so create its profile doc on first use. */
    private suspend fun ensureProfileExists(displayName: String?) {
        val doc = userDoc().get().await()
        if (!doc.exists()) {
            val (first, last) = (displayName ?: "Traveller").split(" ", limit = 2)
                .let { it.getOrElse(0) { "Traveller" } to it.getOrElse(1) { "" } }
            userDoc().set(defaultProfileFields + mapOf("firstName" to first, "lastName" to last, "email" to email)).await()
        }
    }

    // ---------- Profile & settings ----------
    suspend fun loadProfile(): Pair<String, Settings> {
        val d = userDoc().get().await()
        val s = Settings(
            d.getBoolean("darkMode") ?: false, d.getString("currency") ?: "ZAR",
            d.getBoolean("notifications") ?: true, d.getString("defaultOrigin") ?: "JNB",
            d.getString("language") ?: "en"
        )
        return (d.getString("firstName") ?: "Traveller") to s
    }

    suspend fun saveSettings(s: Settings) {
        userDoc().set(
            mapOf("darkMode" to s.darkMode, "currency" to s.currency, "notifications" to s.notifications,
                "defaultOrigin" to s.defaultOrigin, "language" to s.language),
            SetOptions.merge()
        ).await()
    }

    suspend fun saveFirstName(name: String) {
        userDoc().set(mapOf("firstName" to name), SetOptions.merge()).await()
    }

    // ---------- Real-time notifications ----------
    /** Called once after login/register so this device can receive pushes immediately. */
    suspend fun registerForPush() {
        val token = FirebaseMessaging.getInstance().token.await()
        saveFcmToken(token)
    }

    private suspend fun saveFcmToken(token: String) {
        userDoc().set(mapOf("fcmToken" to token), SetOptions.merge()).await()
    }

    /** Called from ProteaMessagingService.onNewToken, which is not a suspend context. */
    fun saveFcmTokenIfSignedIn(token: String) {
        if (!isLoggedIn) return
        backgroundScope.launch { runCatching { saveFcmToken(token) } }
    }

    // ---------- Flights (with offline cache) ----------
    suspend fun seedFlightsIfEmpty() {
        if (!db.collection("flights").limit(1).get().await().isEmpty) return
        val routes = listOf(
            Triple("JNB-CPT", 2450, 125), Triple("JNB-DUR", 1890, 65), Triple("JNB-PLZ", 2100, 100),
            Triple("CPT-DUR", 2300, 120), Triple("JNB-LHR", 9800, 690), Triple("JNB-ZNZ", 4890, 260),
            Triple("JNB-DXB", 7990, 480)
        )
        val airlines = listOf("Protea Air", "Kalahari Air", "Karoo Airways")
        val times = listOf("06:30", "12:15", "18:45")
        val batch = db.batch()
        for ((r, base, dur) in routes) {
            val (a, b) = r.split("-")
            for ((from, to) in listOf(a to b, b to a)) {
                for (i in 0..2) {
                    val dep = times[i]
                    val mins = dep.substring(0, 2).toInt() * 60 + dep.substring(3).toInt() + dur
                    val arr = String.format("%02d:%02d", (mins / 60) % 24, mins % 60)
                    val id = "$from-$to-${i + 1}"
                    batch.set(
                        db.collection("flights").document(id),
                        Flight(id, from, to, airlines[i], dep, arr, dur, base + i * 350 - (if (i == 0) 0 else 0))
                    )
                }
            }
        }
        batch.commit().await()
    }

    /**
     * Offline mode with sync: tries Firestore first. If that fails (or the device has
     * no connection at all), falls back to whatever was cached locally the last time
     * this exact route was searched successfully, so the user can still see results.
     */
    suspend fun searchFlights(from: String, to: String): List<Flight> {
        val routeKey = "$from-$to"
        val dao = database().cachedFlights()
        if (isOnline(requireContext())) {
            try {
                val results = db.collection("flights").whereEqualTo("from", from).whereEqualTo("to", to)
                    .get().await().documents
                    .mapNotNull { it.toObject(Flight::class.java)?.also { f -> f.id = it.id } }
                    .sortedBy { it.price }
                dao.clearRoute(routeKey)
                dao.upsertAll(results.map {
                    CachedFlightEntity(it.id, routeKey, it.from, it.to, it.airline, it.departTime, it.arriveTime, it.durationMin, it.price, System.currentTimeMillis())
                })
                return results
            } catch (_: Exception) {
                // fall through to cache on a transient network error
            }
        }
        return dao.forRoute(routeKey).map { Flight(it.id, it.from, it.to, it.airline, it.departTime, it.arriveTime, it.durationMin, it.price) }
    }

    // ---------- Bookings (queued when offline) ----------
    suspend fun addBooking(b: Booking): Boolean {
        if (isOnline(requireContext())) {
            try {
                userDoc().collection("bookings").add(b).await()
                return true
            } catch (_: Exception) { /* fall through to queue */ }
        }
        database().pendingBookings().queue(
            PendingBookingEntity(0, b.from, b.to, b.airline, b.departTime, b.departDate, b.returnDate,
                b.passengers, b.cabin, b.tripType, b.total, System.currentTimeMillis())
        )
        return false // queued, not yet on the server
    }

    suspend fun bookings(): List<Booking> =
        userDoc().collection("bookings").get().await().documents
            .mapNotNull { it.toObject(Booking::class.java)?.also { b -> b.id = it.id } }
            .sortedBy { it.departDate }
    suspend fun deleteBooking(id: String) { userDoc().collection("bookings").document(id).delete().await() }

    // ---------- Price alerts (queued when offline) ----------
    suspend fun addAlert(from: String, to: String): Boolean {
        if (isOnline(requireContext())) {
            try {
                userDoc().collection("alerts").add(PriceAlert("", from, to)).await()
                return true
            } catch (_: Exception) { /* fall through to queue */ }
        }
        database().pendingAlerts().queue(PendingAlertEntity(0, from, to, System.currentTimeMillis()))
        return false
    }

    suspend fun alerts(): List<PriceAlert> =
        userDoc().collection("alerts").get().await().documents
            .mapNotNull { it.toObject(PriceAlert::class.java)?.also { a -> a.id = it.id } }
    suspend fun deleteAlert(id: String) { userDoc().collection("alerts").document(id).delete().await() }

    // ---------- Sync ----------
    /** Pushes anything queued locally to Firestore. Call when connectivity is regained. */
    suspend fun syncPending(): Int {
        if (!isLoggedIn || !isOnline(requireContext())) return 0
        var synced = 0
        val bookingDao = database().pendingBookings()
        for (pending in bookingDao.all()) {
            userDoc().collection("bookings").add(
                Booking("", pending.from, pending.to, pending.airline, pending.departTime,
                    pending.departDate, pending.returnDate, pending.passengers, pending.cabin,
                    pending.tripType, pending.total)
            ).await()
            bookingDao.remove(pending)
            synced++
        }
        val alertDao = database().pendingAlerts()
        for (pending in alertDao.all()) {
            userDoc().collection("alerts").add(PriceAlert("", pending.from, pending.to)).await()
            alertDao.remove(pending)
            synced++
        }
        return synced
    }

    suspend fun pendingCount(): Int = database().pendingBookings().count()
}
