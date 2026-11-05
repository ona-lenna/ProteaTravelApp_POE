package com.protea.travels.data

import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Pure (Firebase-free, Android-free) business rules, kept out of AppVM so they can be
 * unit-tested on a plain JVM in milliseconds. Each validator returns an error message
 * to show the user, or null when the input is fine.
 */
private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())

fun validateRegistration(first: String, last: String, email: String, pw: String, confirm: String): String? = when {
    first.isBlank() || last.isBlank() -> "Enter your first and last name"
    !isValidEmail(email) -> "Enter a valid email"
    pw.length < 8 -> "Password must be at least 8 characters"
    pw != confirm -> "Passwords do not match"
    else -> null
}

fun validateLogin(email: String, pw: String): String? =
    if (email.isBlank() || pw.isBlank()) "Enter email and password" else null

/** [today] is a parameter so tests don't depend on the real date. */
fun validateSearch(s: SearchParams, today: LocalDate = LocalDate.now()): String? {
    if (s.from.isEmpty() || s.to.isEmpty()) return "Choose origin and destination"
    if (s.from == s.to) return "Origin and destination must differ"
    if (s.depart.isEmpty()) return "Choose a departure date"
    val depart = try { LocalDate.parse(s.depart) } catch (e: Exception) { return "Invalid date" }
    if (depart.isBefore(today)) return "Departure cannot be in the past"
    if (s.tripType == "Return") {
        if (s.ret.isEmpty()) return "Choose a return date"
        val ret = try { LocalDate.parse(s.ret) } catch (e: Exception) { return "Invalid date" }
        if (ret.isBefore(depart)) return "Return must be after departure"
    }
    return null
}

/** Total in ZAR for a flight: per-person price x passengers x cabin multiplier x (2 for return trips). */
fun totalPrice(price: Int, pax: Int, cabin: String, tripType: String): Int =
    (price * pax * (cabinMultiplier[cabin] ?: 1.0) * (if (tripType == "Return") 2 else 1)).roundToInt()
