package com.robnugen.tswa.ui

import com.robnugen.tswa.temperature.Scale

// The hint lines under the temperatures name only the scales that are on.
private fun onlyOne(scale: Scale) = "Only ${scale.symbol} is on — add more in Settings"

private fun on(enabled: Set<Scale>) = Scale.entries.filter { it in enabled }

// Under the converter's big number: "Tap the number to step °C → °F".
fun stepHint(enabled: Set<Scale>): String {
    val scales = on(enabled)
    return if (scales.size == 1) {
        onlyOne(scales[0])
    } else {
        "Tap the number to step " + scales.joinToString(" → ") { it.symbol }
    }
}

// Under the weather: "Tap any temperature to switch °C / °F".
fun switchHint(enabled: Set<Scale>): String {
    val scales = on(enabled)
    return if (scales.size == 1) {
        onlyOne(scales[0])
    } else {
        "Tap any temperature to switch " + scales.joinToString(" / ") { it.symbol }
    }
}

// How a screen reader should say a temperature: "18.8 degrees Celsius",
// "291.9 kelvin". [lead] names it ("Feels like", "High", "Low").
fun spokenTemperature(
    celsius: Double,
    scale: Scale,
    lead: String = "",
): String {
    val unit = if (scale == Scale.K) "kelvin" else "degrees ${scale.fullName}"
    return listOf(lead, scale.format(celsius, withSymbol = false), unit).filter { it.isNotBlank() }.joinToString(" ")
}
