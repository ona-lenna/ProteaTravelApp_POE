package com.protea.travels.data

data class Flight(
    var id: String = "", var from: String = "", var to: String = "",
    var airline: String = "", var departTime: String = "", var arriveTime: String = "",
    var durationMin: Int = 0, var price: Int = 0
)

data class Booking(
    var id: String = "", var from: String = "", var to: String = "", var airline: String = "",
    var departTime: String = "", var departDate: String = "", var returnDate: String = "",
    var passengers: Int = 1, var cabin: String = "Economy", var tripType: String = "Return",
    var total: Int = 0
)

data class PriceAlert(var id: String = "", var from: String = "", var to: String = "")

data class Settings(
    var darkMode: Boolean = false, var currency: String = "ZAR",
    var notifications: Boolean = true, var defaultOrigin: String = "JNB",
    var language: String = "en" // "en" | "zu" | "af" -- see i18n/Strings.kt
)

data class SearchParams(
    val from: String = "JNB", val to: String = "", val tripType: String = "Return",
    val depart: String = "", val ret: String = "", val pax: Int = 2, val cabin: String = "Economy"
)

object Airports {
    val all = linkedMapOf(
        "JNB" to "Johannesburg", "CPT" to "Cape Town", "DUR" to "Durban",
        "PLZ" to "Gqeberha", "LHR" to "London", "ZNZ" to "Zanzibar", "DXB" to "Dubai"
    )
    fun label(code: String) = if (code.isEmpty()) "" else "${all[code] ?: code} ($code)"
    val options get() = all.map { it.key to label(it.key) }
}

val cabinMultiplier = mapOf("Economy" to 1.0, "Premium Economy" to 1.6, "Business" to 2.5)

// Locale.US is explicit so amounts always read "R2,450" as designed, instead of changing
// with the phone's locale (en-ZA would otherwise print "R2 450" with a non-breaking space).
fun money(zar: Int, currency: String): String =
    if (currency == "USD") "US$" + String.format(java.util.Locale.US, "%,d", (zar / 18.5).toInt())
    else "R" + String.format(java.util.Locale.US, "%,d", zar)
