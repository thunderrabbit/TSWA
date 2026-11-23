package com.robnugen.tswa.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// Open-Meteo's free forecast API: no key, CC BY 4.0 (attributed in the app).
object OpenMeteo {
    private const val FORECAST = "https://api.open-meteo.com/v1/forecast"
    private const val TIMEOUT_MS = 15_000
    private const val HTTP_OK = 200

    // The same fields the web version asked for.
    fun forecastUrl(place: Place): String =
        FORECAST +
            "?latitude=${place.latitude}&longitude=${place.longitude}" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,dew_point_2m," +
            "pressure_msl,wind_speed_10m,wind_direction_10m,weather_code,is_day" +
            "&hourly=temperature_2m,weather_code,precipitation_probability,visibility" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset," +
            "uv_index_max,precipitation_probability_max" +
            "&forecast_days=10&timezone=auto"

    // The raw response body. Throws IOException on any network or HTTP
    // failure; the caller falls back to the cache.
    suspend fun fetchForecast(place: Place): String =
        withContext(Dispatchers.IO) {
            val connection = URL(forecastUrl(place)).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                val code = connection.responseCode
                if (code != HTTP_OK) throw IOException("Open-Meteo answered HTTP $code")
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
}
