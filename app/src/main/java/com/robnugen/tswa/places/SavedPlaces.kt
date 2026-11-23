package com.robnugen.tswa.places

import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.Place
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.math.abs

// The saved places list: stored order is display order, newest on top.
object SavedPlaces {
    // Two places are the same if latitude and longitude each match this closely.
    private const val SAME_PLACE_DEGREES = 0.001

    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(Place.serializer())

    fun same(
        a: Place,
        b: Place,
    ): Boolean =
        abs(a.latitude - b.latitude) < SAME_PLACE_DEGREES &&
            abs(a.longitude - b.longitude) < SAME_PLACE_DEGREES

    fun isSaved(
        places: List<Place>,
        place: Place,
    ): Boolean = places.any { same(it, place) }

    // Saving puts the place on top; a place already saved is left alone.
    fun saveOnTop(
        places: List<Place>,
        place: Place,
    ): List<Place> = if (isSaved(places, place)) places else listOf(place) + places

    fun encode(places: List<Place>): String = json.encodeToString(listSerializer, places)

    fun decode(text: String): List<Place> =
        try {
            json.decodeFromString(listSerializer, text)
        } catch (ignoredUnreadable: SerializationException) {
            emptyList() // a list from an unknown layout: start empty rather than crash
        } catch (ignoredUnreadable: IllegalArgumentException) {
            emptyList()
        }
}

// What the old WebView version kept in localStorage, in its own shapes.
object LegacyStorage {
    private val json = Json { ignoreUnknownKeys = true }

    // "tswa-places": [{label, lat, lon, full}], label being the short name.
    @Serializable
    private data class OldPlace(
        val label: String,
        val lat: Double,
        val lon: Double,
        val full: String? = null,
    )

    fun places(text: String?): List<Place> =
        if (text.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                json
                    .decodeFromString(ListSerializer(OldPlace.serializer()), text)
                    .map { Place(it.full ?: it.label, it.label, it.lat, it.lon) }
            } catch (ignoredUnreadable: SerializationException) {
                emptyList() // the old app tolerated junk here too
            } catch (ignoredUnreadable: IllegalArgumentException) {
                emptyList()
            }
        }

    // "tswa-scales": {"C": true, "F": true, "K": false, "R": false}. Null
    // when absent, unreadable, or with every scale off.
    fun scales(text: String?): Set<Scale>? {
        val flags: Map<String, Boolean> =
            try {
                if (text.isNullOrBlank()) emptyMap() else json.decodeFromString(text)
            } catch (ignoredUnreadable: SerializationException) {
                emptyMap() // unreadable: as if nothing was stored
            } catch (ignoredUnreadable: IllegalArgumentException) {
                emptyMap()
            }
        return Scale.entries
            .filter { flags[it.name] == true }
            .toSet()
            .ifEmpty { null }
    }
}
