package com.protea.travels.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ValidationTest {

    // A fixed "today" so these tests give the same result on any day.
    private val today = LocalDate.of(2026, 9, 1)

    private fun search(
        from: String = "JNB", to: String = "CPT", tripType: String = "Return",
        depart: String = "2026-09-15", ret: String = "2026-09-22"
    ) = SearchParams(from = from, to = to, tripType = tripType, depart = depart, ret = ret)

    // ---------------- email ----------------
    @Test fun `valid emails are accepted`() {
        assertTrue(isValidEmail("thabo.dlamini@sandton.co.za"))
        assertTrue(isValidEmail("  a+b@example.com  ")) // surrounding spaces are trimmed
    }

    @Test fun `invalid emails are rejected`() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("thabo@"))
        assertFalse(isValidEmail("no-at-sign.com"))
        assertFalse(isValidEmail("a@b"))
    }

    // ---------------- registration ----------------
    @Test fun `registration with valid input passes`() {
        assertNull(validateRegistration("Thabo", "Dlamini", "t@x.co.za", "password1", "password1"))
    }

    @Test fun `registration rejects blank names`() {
        assertEquals("Enter your first and last name", validateRegistration("", "Dlamini", "t@x.co.za", "password1", "password1"))
        assertEquals("Enter your first and last name", validateRegistration("Thabo", "  ", "t@x.co.za", "password1", "password1"))
    }

    @Test fun `registration rejects a bad email`() {
        assertEquals("Enter a valid email", validateRegistration("Thabo", "Dlamini", "not-an-email", "password1", "password1"))
    }

    @Test fun `registration rejects a short password`() {
        assertEquals("Password must be at least 8 characters", validateRegistration("Thabo", "Dlamini", "t@x.co.za", "short", "short"))
    }

    @Test fun `registration accepts a password of exactly 8 characters`() {
        assertNull(validateRegistration("Thabo", "Dlamini", "t@x.co.za", "12345678", "12345678"))
    }

    @Test fun `registration rejects mismatched passwords`() {
        assertEquals("Passwords do not match", validateRegistration("Thabo", "Dlamini", "t@x.co.za", "password1", "password2"))
    }

    // ---------------- login ----------------
    @Test fun `login needs both email and password`() {
        assertNull(validateLogin("t@x.co.za", "pw"))
        assertEquals("Enter email and password", validateLogin("", "pw"))
        assertEquals("Enter email and password", validateLogin("t@x.co.za", ""))
        assertEquals("Enter email and password", validateLogin("   ", "   "))
    }

    // ---------------- search ----------------
    @Test fun `a valid return search passes`() = assertNull(validateSearch(search(), today))

    @Test fun `a valid one-way search passes without a return date`() =
        assertNull(validateSearch(search(tripType = "One-way", ret = ""), today))

    @Test fun `search needs an origin and a destination`() {
        assertEquals("Choose origin and destination", validateSearch(search(to = ""), today))
        assertEquals("Choose origin and destination", validateSearch(search(from = ""), today))
    }

    @Test fun `origin and destination must differ`() =
        assertEquals("Origin and destination must differ", validateSearch(search(from = "JNB", to = "JNB"), today))

    @Test fun `search needs a departure date`() =
        assertEquals("Choose a departure date", validateSearch(search(depart = ""), today))

    @Test fun `a garbled departure date is reported not crashed on`() =
        assertEquals("Invalid date", validateSearch(search(depart = "15/09/2026"), today))

    @Test fun `departure in the past is rejected`() =
        assertEquals("Departure cannot be in the past", validateSearch(search(depart = "2026-08-31"), today))

    @Test fun `departing today is allowed`() =
        assertNull(validateSearch(search(depart = "2026-09-01", ret = "2026-09-05"), today))

    @Test fun `a return trip needs a return date`() =
        assertEquals("Choose a return date", validateSearch(search(ret = ""), today))

    @Test fun `return before departure is rejected`() =
        assertEquals("Return must be after departure", validateSearch(search(depart = "2026-09-15", ret = "2026-09-10"), today))

    @Test fun `returning on the departure day is allowed`() =
        assertNull(validateSearch(search(depart = "2026-09-15", ret = "2026-09-15"), today))

    @Test fun `a one-way search ignores a bad return date`() =
        assertNull(validateSearch(search(tripType = "One-way", ret = "garbage"), today))
}
