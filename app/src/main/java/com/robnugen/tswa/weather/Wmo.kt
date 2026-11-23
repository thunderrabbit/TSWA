package com.robnugen.tswa.weather

// WMO weather codes, as Open-Meteo reports them, in words. Carried over
// from the web version.
object Wmo {
    private val descriptions: Map<Int, String> =
        mapOf(
            code(code = 0, "Clear sky"),
            code(code = 1, "Mainly clear"),
            code(code = 2, "Partly cloudy"),
            code(code = 3, "Overcast"),
            code(code = 45, "Fog"),
            code(code = 48, "Rime fog"),
            code(code = 51, "Light drizzle"),
            code(code = 53, "Drizzle"),
            code(code = 55, "Dense drizzle"),
            code(code = 61, "Light rain"),
            code(code = 63, "Rain"),
            code(code = 65, "Heavy rain"),
            code(code = 66, "Freezing rain"),
            code(code = 67, "Freezing rain"),
            code(code = 71, "Light snow"),
            code(code = 73, "Snow"),
            code(code = 75, "Heavy snow"),
            code(code = 77, "Snow grains"),
            code(code = 80, "Light showers"),
            code(code = 81, "Showers"),
            code(code = 82, "Violent showers"),
            code(code = 85, "Snow showers"),
            code(code = 86, "Snow showers"),
            code(code = 95, "Thunderstorm"),
            code(code = 96, "Thunderstorm with hail"),
            code(code = 99, "Thunderstorm with hail"),
        )

    private fun code(
        code: Int,
        description: String,
    ) = code to description

    // The web showed an em dash for a code it did not know.
    fun describe(code: Int?): String = descriptions[code] ?: "—"
}
