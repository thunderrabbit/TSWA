package com.robnugen.tswa.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.SerializationException
import java.io.IOException

enum class WeatherStatus {
    // A fetch is under way; any weather shown is the last good one.
    Loading,

    // The weather shown just arrived.
    Fresh,

    // The fetch failed; any weather shown is the last good one.
    Offline,
}

// The weather the app shows and how current it is. Starts from the cache
// so there is something on screen at once; load() then fetches.
class WeatherController(
    private val cache: WeatherCache,
    private val fetch: suspend (Place) -> String = OpenMeteo::fetchForecast,
    private val clock: () -> Long = System::currentTimeMillis,
    // Told why a fetch failed (the app writes it to the system log).
    private val onFailure: (Exception) -> Unit = {},
) {
    var weather by mutableStateOf(cache.load())
        private set

    var status by mutableStateOf(WeatherStatus.Loading)
        private set

    suspend fun load(place: Place) {
        status = WeatherStatus.Loading
        status =
            try {
                val fresh = CachedWeather(place, Forecast.parse(fetch(place)), clock())
                weather = fresh
                cache.save(fresh)
                WeatherStatus.Fresh
            } catch (unreachable: IOException) {
                onFailure(unreachable)
                WeatherStatus.Offline // no network: keep showing the last good data
            } catch (garbled: SerializationException) {
                onFailure(garbled)
                WeatherStatus.Offline // a reply we cannot read: same as no reply
            }
    }
}
