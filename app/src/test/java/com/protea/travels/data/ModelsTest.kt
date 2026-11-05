package com.protea.travels.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {

    @Test fun `default settings are english rand and notifications on`() {
        val s = Settings()
        assertEquals("en", s.language)
        assertEquals("ZAR", s.currency)
        assertEquals("JNB", s.defaultOrigin)
        assertTrue(s.notifications)
        assertFalse(s.darkMode)
    }

    @Test fun `default search is a return trip for two economy adults from Johannesburg`() {
        val p = SearchParams()
        assertEquals("JNB", p.from)
        assertEquals("", p.to)
        assertEquals("Return", p.tripType)
        assertEquals(2, p.pax)
        assertEquals("Economy", p.cabin)
    }

    @Test fun `airport labels combine city and code`() {
        assertEquals("Johannesburg (JNB)", Airports.label("JNB"))
        assertEquals("Cape Town (CPT)", Airports.label("CPT"))
    }

    @Test fun `an empty airport code gives an empty label`() = assertEquals("", Airports.label(""))

    @Test fun `an unknown airport code is shown as-is`() = assertEquals("XYZ (XYZ)", Airports.label("XYZ"))

    @Test fun `picker options cover every airport`() {
        assertEquals(Airports.all.size, Airports.options.size)
        assertTrue(Airports.options.any { it.first == "DUR" && it.second == "Durban (DUR)" })
    }

    @Test fun `copying settings changes only the field asked for`() {
        val s = Settings().copy(language = "zu")
        assertEquals("zu", s.language)
        assertEquals("ZAR", s.currency)
    }
}
