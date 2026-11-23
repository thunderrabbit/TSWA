package com.robnugen.tswa.weather

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

// The last good forecast, with the place it was for and when it came.
@Serializable
data class CachedWeather(
    val place: Place,
    val forecast: Forecast,
    val fetchedAtMillis: Long,
)

// Keeps the last good forecast in one file, so the app has something to
// show the moment it opens, online or not.
class WeatherCache(
    private val file: File,
) {
    private val json = Json { ignoreUnknownKeys = true }

    // Null when there is no cache yet or it cannot be read; the app then
    // simply waits for a fresh fetch.
    fun load(): CachedWeather? {
        if (!file.exists()) return null
        return try {
            json.decodeFromString(CachedWeather.serializer(), file.readText())
        } catch (ignoredUnreadable: SerializationException) {
            null // a cache from an older layout: start without one
        } catch (ignoredUnreadable: IOException) {
            null // cache file unreadable: start without one
        }
    }

    // Best effort: a cache that cannot be written only costs the instant
    // start next time.
    fun save(weather: CachedWeather) {
        try {
            file.writeText(json.encodeToString(CachedWeather.serializer(), weather))
        } catch (ignoredUnwritable: IOException) {
            // cache not written: next start waits for the network instead
        }
    }
}
