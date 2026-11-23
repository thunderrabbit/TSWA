package com.robnugen.tswa.weather

import kotlinx.serialization.Serializable

// A place the weather is for: "Adelaide, Australia" in full, "Adelaide"
// for short (chips, the save button).
@Serializable
data class Place(
    val fullName: String,
    val shortName: String,
    val latitude: Double,
    val longitude: Double,
) {
    companion object {
        // Where TSWA looks when it has nowhere else to look.
        val ADELAIDE = Place("Adelaide, Australia", "Adelaide", latitude = -34.9285, longitude = 138.6007)
    }
}
