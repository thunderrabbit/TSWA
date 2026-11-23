package com.robnugen.tswa.places

import com.robnugen.tswa.weather.Place

// The first-run question about location (mg #899).
object Onboarding {
    // Ask when the person has never been asked and has no places yet (a
    // fresh install), or when they emptied their list to be asked again.
    // Someone who already has saved places (Rob, coming from the WebView
    // version) was onboarded there and is not asked.
    fun needed(
        onboarded: Boolean?,
        savedIsEmpty: Boolean,
    ): Boolean = onboarded == false || (onboarded == null && savedIsEmpty)

    // The person's own location, named from the forecast's timezone:
    // "Australia/Adelaide" -> "Your location near Adelaide", "Adelaide".
    fun placeFromTimezone(
        timezone: String,
        latitude: Double,
        longitude: Double,
    ): Place {
        val town = timezone.substringAfterLast('/').replace('_', ' ').ifBlank { "here" }
        return Place("Your location near $town", town, latitude, longitude)
    }
}
