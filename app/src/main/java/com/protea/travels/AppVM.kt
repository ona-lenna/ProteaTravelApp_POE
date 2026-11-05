package com.protea.travels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.protea.travels.data.*
import com.protea.travels.i18n.Language
import com.protea.travels.i18n.stringsFor
import kotlinx.coroutines.launch

class AppVM : ViewModel() {
    var message by mutableStateOf<String?>(null)
    var busy by mutableStateOf(false)
    var userName by mutableStateOf("")
    var settings by mutableStateOf(Settings())
    var search by mutableStateOf(SearchParams())
    var results by mutableStateOf<List<Flight>>(emptyList())
    var bookings by mutableStateOf<List<Booking>>(emptyList())
    var alerts by mutableStateOf<List<PriceAlert>>(emptyList())

    // Multi-language support: current language, driven by Settings and readable by
    // every screen via i18n.LocalStrings (wired in MainActivity).
    var language by mutableStateOf(Language.ENGLISH)

    // Weather feature: current conditions for the selected destination airport.
    var weather by mutableStateOf<WeatherInfo?>(null)
    var weatherLoading by mutableStateOf(false)

    // Offline mode with sync: whether the device currently has no connection, and
    // how many locally-queued actions (bookings/alerts) are still waiting to sync.
    var isOffline by mutableStateOf(false)
    var pendingSyncCount by mutableStateOf(0)

    init { if (Repo.isLoggedIn) run { afterAuth() } }

    private fun run(block: suspend () -> Unit) {
        viewModelScope.launch {
            busy = true
            try { block() } catch (e: Exception) { message = e.localizedMessage ?: "Something went wrong" }
            busy = false
        }
    }

    private suspend fun afterAuth() {
        val (name, s) = Repo.loadProfile()
        userName = name; settings = s
        language = Language.entries.firstOrNull { it.code == s.language } ?: Language.ENGLISH
        search = search.copy(from = s.defaultOrigin)
        try { Repo.seedFlightsIfEmpty() } catch (_: Exception) {}
        bookings = Repo.bookings(); alerts = Repo.alerts()
        pendingSyncCount = runCatching { Repo.pendingCount() }.getOrDefault(0)
        // Best-effort: a missing/blocked notification permission should never break sign-in.
        runCatching { Repo.registerForPush() }
    }

    // ---------- Auth ----------
    fun register(first: String, last: String, email: String, pw: String, confirm: String, onOk: () -> Unit) {
        validateRegistration(first, last, email, pw, confirm)?.let { message = it; return }
        run { Repo.register(first.trim(), last.trim(), email.trim(), pw); afterAuth(); onOk() }
    }

    fun login(email: String, pw: String, onOk: () -> Unit) {
        validateLogin(email, pw)?.let { message = it; return }
        run { Repo.login(email.trim(), pw); afterAuth(); onOk() }
    }

    // ---------- Single sign-on ----------
    fun signInWithGoogle(idToken: String, onOk: () -> Unit) =
        run { Repo.signInWithGoogle(idToken); afterAuth(); onOk() }

    fun signInWithFacebook(accessToken: String, onOk: () -> Unit) =
        run { Repo.signInWithFacebook(accessToken); afterAuth(); onOk() }

    fun forgotPassword(email: String) {
        if (email.isBlank()) { message = "Enter your email first"; return }
        run { Repo.resetPassword(email.trim()); message = "Password reset email sent" }
    }

    fun logout() {
        Repo.logout(); bookings = emptyList(); alerts = emptyList(); results = emptyList()
        userName = ""; settings = Settings(); search = SearchParams(); language = Language.ENGLISH
        weather = null; pendingSyncCount = 0
    }

    // ---------- Settings ----------
    fun updateSettings(s: Settings) {
        settings = s
        language = Language.entries.firstOrNull { it.code == s.language } ?: Language.ENGLISH
        run { Repo.saveSettings(s) }
    }
    fun saveName(name: String) {
        if (name.isBlank()) { message = "Name cannot be empty"; return }
        run { Repo.saveFirstName(name.trim()); userName = name.trim(); message = "Profile updated" }
    }

    // ---------- Search & booking ----------
    fun updateSearch(s: SearchParams) {
        val destChanged = s.to != search.to
        search = s
        if (destChanged && s.to.isNotEmpty()) loadWeather(s.to)
    }

    private fun loadWeather(airportCode: String) {
        viewModelScope.launch {
            weatherLoading = true
            weather = runCatching { WeatherRepo.current(airportCode) }.getOrNull()
            weatherLoading = false
        }
    }

    fun runSearch(onDone: () -> Unit) {
        val s = search
        validateSearch(s)?.let { message = it; return }
        run {
            results = Repo.searchFlights(s.from, s.to)
            if (results.isEmpty()) message = "No flights found for that route"
            else if (isOffline) message = stringsFor(language).offlineCached
            onDone()
        }
    }

    fun total(f: Flight): Int = totalPrice(f.price, search.pax, search.cabin, search.tripType)

    fun book(f: Flight, onDone: () -> Unit) {
        val s = search
        run {
            val syncedImmediately = Repo.addBooking(
                Booking("", f.from, f.to, f.airline, f.departTime, s.depart,
                    if (s.tripType == "Return") s.ret else "", s.pax, s.cabin, s.tripType, total(f))
            )
            if (syncedImmediately) {
                bookings = Repo.bookings(); message = "Booking confirmed!"
            } else {
                pendingSyncCount = Repo.pendingCount(); message = stringsFor(language).offlineQueued
            }
            onDone()
        }
    }

    fun cancelBooking(id: String) = run { Repo.deleteBooking(id); bookings = Repo.bookings(); message = "Booking cancelled" }

    fun addAlert() {
        val s = search
        if (s.to.isEmpty() || s.from == s.to) { message = "Pick a valid route first"; return }
        if (alerts.any { it.from == s.from && it.to == s.to }) { message = "Alert already exists"; return }
        run {
            val syncedImmediately = Repo.addAlert(s.from, s.to)
            if (syncedImmediately) { alerts = Repo.alerts(); message = "Price alert added" }
            else { pendingSyncCount = Repo.pendingCount(); message = stringsFor(language).offlineQueued }
        }
    }
    fun removeAlert(id: String) = run { Repo.deleteAlert(id); alerts = Repo.alerts() }

    // ---------- Connectivity / offline sync ----------
    /** Called from ProteaApp whenever Connectivity.observe() reports a change. */
    fun onConnectivityChanged(online: Boolean) {
        val wasOffline = isOffline
        isOffline = !online
        if (online && wasOffline && Repo.isLoggedIn) {
            run {
                val synced = Repo.syncPending()
                pendingSyncCount = Repo.pendingCount()
                if (synced > 0) {
                    bookings = Repo.bookings(); alerts = Repo.alerts()
                    message = stringsFor(language).syncedNow
                }
            }
        }
    }
}
