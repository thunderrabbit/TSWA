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
    private val geocode: suspend (String) -> String = OpenMeteo::fetchGeocoding,
    private val clock: () -> Long = System::currentTimeMillis,
    // Told why a fetch failed (the app writes it to the system log).
    private val onFailure: (Exception) -> Unit = {},
) {
    var weather by mutableStateOf(cache.load())
        private set

    var status by mutableStateOf(WeatherStatus.Loading)
        private set

    // The place the person is looking at. It changes as soon as a place
    // is chosen, before its weather arrives, so it can be saved at once.
    var currentPlace by mutableStateOf(weather?.place ?: Place.ADELAIDE)
        private set

    // What the last search has to say ("Searching…", nothing found, no
    // network); null when there is nothing to say. Shown in the status line.
    var searchNote by mutableStateOf<String?>(null)
        private set

    suspend fun load(place: Place) {
        currentPlace = place
        searchNote = null
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

    // Look a place up by name and load its weather: the first match wins.
    suspend fun search(query: String) {
        val name = query.trim()
        if (name.isEmpty()) return
        searchNote = "Searching…"
        val lookup: Result<Place?> =
            try {
                Result.success(Geocoding.firstPlace(geocode(name)))
            } catch (unreachable: IOException) {
                Result.failure(unreachable)
            } catch (garbled: SerializationException) {
                Result.failure(garbled)
            }
        lookup.fold(
            onSuccess = { place ->
                if (place == null) searchNote = "No place found for “$name”." else load(place)
            },
            onFailure = { problem ->
                onFailure(problem as Exception)
                searchNote = "Search failed (offline?)."
            },
        )
    }
}
