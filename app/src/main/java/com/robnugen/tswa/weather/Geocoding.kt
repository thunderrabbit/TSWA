package com.robnugen.tswa.weather

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Open-Meteo's geocoding reply. With no match the "results" key is
// missing altogether.
@Serializable
private data class GeocodingReply(
    val results: List<GeocodingResult> = emptyList(),
)

@Serializable
private data class GeocodingResult(
    val name: String,
    val country: String? = null,
    // State, prefecture, region; then county, district, city.
    val admin1: String? = null,
    val admin2: String? = null,
    val latitude: Double,
    val longitude: Double,
)

// One place a search found, with enough of where it is ([area]: district,
// state, country) to tell two towns of the same name apart.
data class SearchMatch(
    val place: Place,
    val area: String,
)

object Geocoding {
    private val json = Json { ignoreUnknownKeys = true }

    // Every place in the reply, best match first; empty when nothing
    // matched. Shown as "Name, State, Country" in full, "Name" for short.
    fun matches(text: String): List<SearchMatch> =
        json.decodeFromString(GeocodingReply.serializer(), text).results.map { r ->
            val full = listOf(r.name, r.admin1, r.country).named().joinToString(", ")
            val area = listOf(r.admin2, r.admin1, r.country).named().joinToString(", ")
            SearchMatch(Place(full, r.name, r.latitude, r.longitude), area)
        }

    // The parts that say something: no blanks, no repeats ("Tokyo, Tokyo").
    private fun List<String?>.named(): List<String> = filterNotNull().filter { it.isNotBlank() }.distinct()
}
