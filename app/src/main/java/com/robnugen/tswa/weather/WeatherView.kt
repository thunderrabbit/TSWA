package com.robnugen.tswa.weather

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

// How a weather icon is coloured: sunny amber, wet cyan, otherwise grey.
enum class Tone { Sun, Rain, Cloud }

// The weather icons, each with its colour, as on the web version.
enum class Glyph(
    val tone: Tone,
) {
    Sun(Tone.Sun),
    Moon(Tone.Cloud),
    Partly(Tone.Sun),
    PartlyNight(Tone.Cloud),
    Cloud(Tone.Cloud),
    Fog(Tone.Cloud),
    Drizzle(Tone.Rain),
    Rain(Tone.Rain),
    Snow(Tone.Cloud),
    Thunder(Tone.Rain),
    ;

    companion object {
        // WMO code to icon, the web version's glyphFor().
        @Suppress("MagicNumber") // WMO weather code ranges
        fun forCode(
            code: Int?,
            isDay: Boolean = true,
        ): Glyph =
            when (code) {
                0 -> if (isDay) Sun else Moon
                1, 2 -> if (isDay) Partly else PartlyNight
                3 -> Cloud
                45, 48 -> Fog
                in 51..55 -> Drizzle
                in 61..67, in 80..82 -> Rain
                in 71..77, 85, 86 -> Snow
                null -> Cloud
                else -> if (code >= 95) Thunder else Cloud
            }
    }
}

data class HourSlot(
    val label: String,
    val glyph: Glyph,
    val temperatureC: Double?,
    val rainPercent: Int?,
)

// One row of the 10-day list. barStart and barWidth place the low..high
// bar on the ten days' overall range, as fractions of the bar's track.
data class DaySlot(
    val name: String,
    val glyph: Glyph,
    val rainPercent: Int?,
    val lowC: Double?,
    val highC: Double?,
    val barStart: Float,
    val barWidth: Float,
)

// Turns a Forecast into what the Weather page shows. No Android here, so
// it is all unit-tested.
object WeatherView {
    private const val HOURS_SHOWN = 12
    private const val HOUR_PREFIX = 13 // "2026-11-23T11" from "2026-11-23T11:15"
    private const val CLOCK_START = 11 // "11:00" inside "2026-11-23T11:00"
    private const val CLOCK_END = 16
    private const val MIN_BAR_SHARE = 0.04f // the web's 4% minimum width
    private const val RAIN_WORTH_MENTIONING = 20
    private const val METRES_PER_KM = 1000.0

    // Where the hourly strip starts: the hour the PLACE is in now, found
    // by matching Open-Meteo's own timestamps, never the phone's clock.
    fun currentHourIndex(forecast: Forecast): Int {
        val hour = forecast.current.time.take(HOUR_PREFIX)
        return forecast.hourly.time
            .indexOfFirst { it.take(HOUR_PREFIX) == hour }
            .coerceAtLeast(0)
    }

    fun hours(forecast: Forecast): List<HourSlot> {
        val h = forecast.hourly
        val start = currentHourIndex(forecast)
        return (start until minOf(start + HOURS_SHOWN, h.time.size)).map { i ->
            val now = i == start
            HourSlot(
                label = if (now) "Now" else h.time[i].substring(CLOCK_START, CLOCK_END),
                glyph = Glyph.forCode(h.weatherCode.getOrNull(i), isDay = isDay(forecast, i, now)),
                temperatureC = h.temperatureC.getOrNull(i),
                rainPercent = h.rainPercent.getOrNull(i),
            )
        }
    }

    // "Now" goes by the current conditions; later hours by their own
    // is_day, or by day when a cached forecast from before mg #907 has none.
    private fun isDay(
        forecast: Forecast,
        i: Int,
        now: Boolean,
    ): Boolean =
        if (now) {
            forecast.current.isDay == 1
        } else {
            forecast.hourly.isDay.getOrNull(i) != 0
        }

    fun days(
        forecast: Forecast,
        locale: Locale = Locale.getDefault(),
    ): List<DaySlot> {
        val d = forecast.daily
        val lows = d.lowC.filterNotNull()
        val highs = d.highC.filterNotNull()
        val weekMin = lows.minOrNull() ?: 0.0
        val span = max(1.0, (highs.maxOrNull() ?: weekMin) - weekMin)
        return d.time.indices.map { i ->
            val low = d.lowC.getOrNull(i)
            val high = d.highC.getOrNull(i)
            val start = if (low == null) 0f else ((low - weekMin) / span).toFloat()
            val width = if (low == null || high == null) 0f else ((high - low) / span).toFloat()
            DaySlot(
                name = if (i == 0) "Today" else weekday(d.time[i], locale),
                glyph = Glyph.forCode(d.weatherCode.getOrNull(i)),
                rainPercent = d.rainPercentMax.getOrNull(i),
                lowC = low,
                highC = high,
                barStart = start,
                barWidth = max(MIN_BAR_SHARE, width),
            )
        }
    }

    private fun weekday(
        isoDate: String,
        locale: Locale,
    ): String = LocalDate.parse(isoDate).dayOfWeek.getDisplayName(TextStyle.SHORT, locale)

    // Today's chance of rain, and whether it is worth a mention next to
    // the high and low.
    fun rainToday(forecast: Forecast): Int = forecast.daily.rainPercentMax.firstOrNull() ?: 0

    fun rainWorthMentioning(percent: Int): Boolean = percent >= RAIN_WORTH_MENTIONING

    // Visibility for the current hour, in km and miles.
    fun visibilityKm(forecast: Forecast): Double {
        val metres = forecast.hourly.visibility.getOrNull(currentHourIndex(forecast)) ?: 0.0
        return metres / METRES_PER_KM
    }

    // "05:57" from "2026-11-23T05:57".
    fun clockTime(isoDateTime: String?): String = isoDateTime?.substring(CLOCK_START, CLOCK_END) ?: "–"
}

// The details grid's numbers: metric first, the US equivalent beneath.
object Measures {
    private const val DEGREES_PER_COMPASS_POINT = 45.0
    private const val MPH_PER_KMH = 0.621
    private const val MILES_PER_KM = 0.621
    private const val INHG_PER_HPA = 0.02953
    private val compass = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

    fun compassPoint(degrees: Double?): String {
        val point = ((degrees ?: 0.0) / DEGREES_PER_COMPASS_POINT).roundToInt()
        return compass[point.mod(compass.size)]
    }

    @Suppress("MagicNumber") // the UV index's own bands
    fun uvWord(uv: Double): String =
        when {
            uv < 3 -> "Low"
            uv < 6 -> "Moderate"
            uv < 8 -> "High"
            uv < 11 -> "Very high"
            else -> "Extreme"
        }

    fun mph(kmh: Double): Int = (kmh * MPH_PER_KMH).roundToInt()

    fun miles(km: Double): Int = (km * MILES_PER_KM).roundToInt()

    fun inHg(hpa: Double): String = "%.2f".format(Locale.US, hpa * INHG_PER_HPA)
}
