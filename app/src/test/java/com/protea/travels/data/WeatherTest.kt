package com.protea.travels.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherTest {

    @Test fun `weather codes map to readable conditions`() {
        assertEquals("Clear sky", wmoToCondition(0))
        assertEquals("Partly cloudy", wmoToCondition(2))
        assertEquals("Fog", wmoToCondition(45))
        assertEquals("Drizzle", wmoToCondition(53))
        assertEquals("Rain", wmoToCondition(61))
        assertEquals("Snow", wmoToCondition(73))
        assertEquals("Showers", wmoToCondition(81))
        assertEquals("Thunderstorm", wmoToCondition(95))
    }

    @Test fun `an unrecognised weather code is reported as unsettled`() =
        assertEquals("Unsettled", wmoToCondition(999))

    @Test fun `a normal API response is parsed`() {
        val body = """{"latitude":-33.97,"current_weather":{"temperature":21.4,"windspeed":14.2,"weathercode":2}}"""
        assertEquals(WeatherInfo(tempC = 21, condition = "Partly cloudy", windKmh = 14), parseWeather(body))
    }

    @Test fun `below-zero temperatures are parsed`() {
        val body = """{"current_weather":{"temperature":-3.0,"windspeed":5.0,"weathercode":71}}"""
        assertEquals(-3, parseWeather(body)!!.tempC)
        assertEquals("Snow", parseWeather(body)!!.condition)
    }

    @Test fun `a response without current_weather gives null`() =
        assertNull(parseWeather("""{"error":true,"reason":"bad request"}"""))

    @Test fun `a response with a missing field gives null`() =
        assertNull(parseWeather("""{"current_weather":{"temperature":21.4}}"""))

    @Test fun `text that is not JSON gives null instead of crashing`() {
        assertNull(parseWeather("<html>502 Bad Gateway</html>"))
        assertNull(parseWeather(""))
    }

    @Test fun `every airport the app offers has weather coordinates`() {
        for (code in Airports.all.keys) assertNotNull("No coordinates for $code", AirportCoords[code])
    }

    @Test fun `coordinates are valid latitudes and longitudes`() {
        for ((code, c) in AirportCoords) {
            assertTrue("Bad latitude for $code", c.first in -90.0..90.0)
            assertTrue("Bad longitude for $code", c.second in -180.0..180.0)
        }
    }
}
