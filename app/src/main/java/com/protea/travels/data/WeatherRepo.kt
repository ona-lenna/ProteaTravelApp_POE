package com.protea.travels.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

data class WeatherInfo(val tempC: Int, val condition: String, val windKmh: Int)

/** (lat, lon) per destination, used only to fetch that city's current weather. */
val AirportCoords = mapOf(
    "JNB" to (-26.1392 to 28.2460), "CPT" to (-33.9715 to 18.6021), "DUR" to (-29.6197 to 31.1197),
    "PLZ" to (-33.9849 to 25.6173), "LHR" to (51.4700 to -0.4543), "ZNZ" to (-6.2220 to 39.2249),
    "DXB" to (25.2532 to 55.3657)
)

internal fun wmoToCondition(code: Int): String = when (code) {
    0 -> "Clear sky"
    1, 2, 3 -> "Partly cloudy"
    45, 48 -> "Fog"
    51, 53, 55, 56, 57 -> "Drizzle"
    61, 63, 65, 66, 67 -> "Rain"
    71, 73, 75, 77 -> "Snow"
    80, 81, 82 -> "Showers"
    95, 96, 99 -> "Thunderstorm"
    else -> "Unsettled"
}

/**
 * Weather feature: calls Open-Meteo (https://open-meteo.com), a free weather API that
 * needs no API key, so there is nothing for a marker to configure to see it work.
 */
object WeatherRepo {
    private val client = OkHttpClient()

    suspend fun current(airportCode: String): WeatherInfo? = withContext(Dispatchers.IO) {
        val coords = AirportCoords[airportCode] ?: return@withContext null
        val (lat, lon) = coords
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
        try {
            client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful) return@withContext null
                val body = resp.body?.string() ?: return@withContext null
                parseWeather(body)
            }
        } catch (_: IOException) {
            null
        } catch (_: Exception) {
            null
        }
    }
}

/** Turns an Open-Meteo response body into a WeatherInfo, or null if it isn't in the expected shape. */
internal fun parseWeather(body: String): WeatherInfo? = try {
    val current = JSONObject(body).getJSONObject("current_weather")
    WeatherInfo(
        tempC = current.getDouble("temperature").toInt(),
        windKmh = current.getDouble("windspeed").toInt(),
        condition = wmoToCondition(current.getInt("weathercode"))
    )
} catch (_: Exception) {
    null
}
