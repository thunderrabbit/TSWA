package com.robnugen.tswa.places

import com.robnugen.tswa.weather.Place
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

    // A drag drops the place from [from] into position [to].
    fun move(
        places: List<Place>,
        from: Int,
        to: Int,
    ): List<Place> {
        if (from !in places.indices || to !in places.indices) return places
        val moved = places.toMutableList()
        moved.add(to, moved.removeAt(from))
        return moved
    }

    // Undo puts a deleted place back where it was (or at the end).
    fun restore(
        places: List<Place>,
        place: Place,
        index: Int,
    ): List<Place> =
        if (isSaved(places, place)) {
            places
        } else {
            places.toMutableList().apply { add(index.coerceIn(0, size), place) }
        }

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
