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
    val latitude: Double,
    val longitude: Double,
)

object Geocoding {
    private val json = Json { ignoreUnknownKeys = true }

    // The first place in the reply, or null when nothing matched. Shown
    // as "Name, Country" in full, "Name" for short.
    fun firstPlace(text: String): Place? {
        val first = json.decodeFromString(GeocodingReply.serializer(), text).results.firstOrNull() ?: return null
        val full = if (first.country.isNullOrBlank()) first.name else "${first.name}, ${first.country}"
        return Place(full, first.name, first.latitude, first.longitude)
    }
}
