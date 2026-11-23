package com.robnugen.tswa.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.robnugen.tswa.places.SavedPlaces
import kotlinx.serialization.SerializationException
import java.io.IOException

// A search's message; a [problem] is shown in a warning colour.
data class SearchNote(
    val text: String,
    val problem: Boolean = false,
) {
    companion object {
        fun problem(text: String) = SearchNote(text, problem = true)
    }
}

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
    // network); null when there is nothing to say. Shown under the search box.
    var searchNote by mutableStateOf<SearchNote?>(null)
        private set

    // Same-named places the last search found, for the person to pick
    // from; empty when there is nothing to choose.
    var choices by mutableStateOf<List<SearchMatch>>(emptyList())
        private set

    // The person is typing again or went elsewhere: the last search's
    // message and choices no longer apply.
    fun clearSearch() {
        searchNote = null
        choices = emptyList()
    }

    // Coming back to an app Android kept alive shows what was loaded
    // then: fetch again if that is older than [STALE_AFTER_MILLIS]. Not
    // while a fetch is under way (a cold start is already loading).
    suspend fun refreshIfStale() {
        if (status == WeatherStatus.Loading) return
        val age = weather?.let { clock() - it.fetchedAtMillis }
        if (age == null || age >= STALE_AFTER_MILLIS) load(currentPlace)
    }

    companion object {
        const val STALE_AFTER_MILLIS = 15 * 60 * 1000L
    }

    // Fetch the weather for [place]. [rename] may name the place from the
    // forecast itself (onboarding names the person's location that way).
    suspend fun load(
        place: Place,
        rename: (Forecast) -> Place = { place },
    ) {
        currentPlace = place
        clearSearch()
        status = WeatherStatus.Loading
        status =
            try {
                val forecast = Forecast.parse(fetch(place))
                val named = rename(forecast)
                currentPlace = named
                val fresh = CachedWeather(named, forecast, clock())
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

    // Look a place up by name. One new match loads at once; several
    // become [choices]. Places already in [saved] are not offered again,
    // but if they are all there is, the first is shown.
    suspend fun search(
        query: String,
        saved: List<Place> = emptyList(),
    ) {
        val name = query.trim()
        if (name.isEmpty()) return
        clearSearch()
        searchNote = SearchNote("Searching…")
        val lookup: Result<List<SearchMatch>> =
            try {
                Result.success(Geocoding.matches(geocode(name)))
            } catch (unreachable: IOException) {
                Result.failure(unreachable)
            } catch (garbled: SerializationException) {
                Result.failure(garbled)
            }
        lookup.fold(
            onSuccess = { matches -> offer(name, matches, saved) },
            onFailure = { problem ->
                onFailure(problem as Exception)
                searchNote = SearchNote.problem("Search failed (offline?).")
            },
        )
    }

    private suspend fun offer(
        name: String,
        matches: List<SearchMatch>,
        saved: List<Place>,
    ) {
        val fresh =
            matches
                .filterNot { SavedPlaces.isSaved(saved, it.place) }
                .distinctBy { it.place.latitude to it.place.longitude }
        when {
            matches.isEmpty() -> {
                searchNote = SearchNote.problem("No place found for “$name”.")
            }

            fresh.isEmpty() -> {
                load(matches.first().place)
            }

            fresh.size == 1 -> {
                load(fresh.single().place)
            }

            else -> {
                choices = fresh
                searchNote = SearchNote("Which “$name”?")
            }
        }
    }
}
