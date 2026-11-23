package com.robnugen.tswa.weather

import com.robnugen.tswa.BuildConfig
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
    private const val ERROR_BODY_CHARS = 300

    // Android's default User-Agent ("Dalvik/2.1.0 ...") gets HTTP 403 from
    // Open-Meteo for a request this size (found 2026-11-23; a short query
    // passes). The WebView never met it because it sent a browser's. Say
    // honestly which app is asking instead.
    val USER_AGENT = "TSWA/${BuildConfig.VERSION_NAME} (Android; +https://github.com/thunderrabbit/TSWA)"

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
                connection.setRequestProperty("User-Agent", USER_AGENT)
                val code = connection.responseCode
                if (code != HTTP_OK) {
                    val why = connection.errorStream?.use { it.reader().readText() }.orEmpty()
                    throw IOException(
                        "Open-Meteo answered HTTP $code for ${connection.url}: ${why.take(ERROR_BODY_CHARS)}",
                    )
                }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
}
