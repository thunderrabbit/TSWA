package com.robnugen.tswa.weather

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class WeatherViewTest {
    private val sample =
        Forecast.parse(checkNotNull(javaClass.classLoader?.getResource("open-meteo-adelaide.json")).readText())

    @Test
    fun hourlyStartsAtThePlacesCurrentHour() {
        // The sample's current time is 2026-11-23T11:15, Adelaide time.
        assertEquals(11, WeatherView.currentHourIndex(sample))
        val hours = WeatherView.hours(sample)
        assertEquals(12, hours.size)
        assertEquals("Now", hours[0].label)
        assertEquals("12:00", hours[1].label)
        assertEquals("22:00", hours[11].label)
    }

    @Test
    fun hoursAfterSunsetShowNightIcons() {
        // Clear sky all day; dark from 20:00 (index 20) on, light again from 06:00 next day.
        val n = sample.hourly.time.size
        val clear = sample.hourly.copy(weatherCode = List(n) { 0 }, isDay = List(n) { i -> if (i % 24 in 6..19) 1 else 0 })
        val hours = WeatherView.hours(sample.copy(hourly = clear))
        assertEquals(Glyph.Sun, hours[0].glyph) // Now, 11:15, current.is_day = 1
        assertEquals(Glyph.Sun, hours[8].glyph) // 19:00
        assertEquals(Glyph.Moon, hours[9].glyph) // 20:00
        assertEquals(Glyph.Moon, hours[11].glyph) // 22:00
    }

    @Test
    fun partlyCloudyNightsGetTheirOwnIcon() {
        assertEquals(Glyph.Partly, Glyph.forCode(2, isDay = true))
        assertEquals(Glyph.PartlyNight, Glyph.forCode(2, isDay = false))
    }

    @Test
    fun aCachedForecastWithoutIsDayShowsDayIcons() {
        val n = sample.hourly.time.size
        val old = sample.hourly.copy(weatherCode = List(n) { 0 }, isDay = emptyList())
        assertTrue(WeatherView.hours(sample.copy(hourly = old)).all { it.glyph == Glyph.Sun })
    }

    @Test
    fun hourlyFallsBackToTheFirstHourWhenTheClockDoesNotMatch() {
        val odd = sample.copy(current = sample.current.copy(time = "1999-01-01T00:00"))
        assertEquals(0, WeatherView.currentHourIndex(odd))
    }

    @Test
    fun tenDaysStartWithTodayThenWeekdays() {
        val days = WeatherView.days(sample, Locale.ENGLISH)
        assertEquals(10, days.size)
        assertEquals("Today", days[0].name)
        assertEquals("Tue", days[1].name) // 2026-11-24
        assertEquals("Wed", days[9].name) // 2026-12-02
    }

    @Test
    fun rangeBarsSitOnTheTenDayRange() {
        val days = WeatherView.days(sample, Locale.ENGLISH)
        days.forEach {
            assertTrue(it.barStart >= 0f)
            assertTrue(it.barWidth >= 0.04f)
            assertTrue(it.barStart + it.barWidth <= 1.0001f)
        }
        // The coldest night starts its bar at the left end; the hottest
        // day ends its bar at the right end.
        assertEquals(0f, days.minOf { it.barStart }, 1e-6f)
        assertEquals(1f, days.maxOf { it.barStart + it.barWidth }, 1e-6f)
    }

    @Test
    fun picksGlyphsLikeTheWebVersion() {
        assertEquals(Glyph.Sun, Glyph.forCode(0, isDay = true))
        assertEquals(Glyph.Moon, Glyph.forCode(0, isDay = false))
        assertEquals(Glyph.Partly, Glyph.forCode(2))
        assertEquals(Glyph.Fog, Glyph.forCode(48))
        assertEquals(Glyph.Drizzle, Glyph.forCode(53))
        assertEquals(Glyph.Rain, Glyph.forCode(81))
        assertEquals(Glyph.Snow, Glyph.forCode(86))
        assertEquals(Glyph.Thunder, Glyph.forCode(99))
        assertEquals(Glyph.Cloud, Glyph.forCode(42))
        assertEquals(Tone.Rain, Glyph.Drizzle.tone)
        assertEquals(Tone.Sun, Glyph.Partly.tone)
    }

    @Test
    fun theDetailsGridNumbers() {
        assertEquals("N", Measures.compassPoint(0.0))
        assertEquals("N", Measures.compassPoint(350.0))
        assertEquals("W", Measures.compassPoint(248.0))
        assertEquals("SW", Measures.compassPoint(225.0))
        assertEquals("Very high", Measures.uvWord(9.0))
        assertEquals("Low", Measures.uvWord(2.9))
        assertEquals("Extreme", Measures.uvWord(11.0))
        assertEquals(13, Measures.mph(21.6)) // shown as "22 km/h" above it
        assertEquals(14, Measures.mph(22.0))
        assertEquals("29.99", Measures.inHg(1015.7)) // shown as "1016 hPa" above it
        assertEquals("30.00", Measures.inHg(1016.0))
        assertEquals(35, Measures.miles(56.0))
        assertEquals("05:57", WeatherView.clockTime("2026-11-23T05:57"))
    }

    @Test
    fun rainIsMentionedFromTwentyPercent() {
        assertTrue(WeatherView.rainWorthMentioning(20))
        assertFalse(WeatherView.rainWorthMentioning(19))
    }
}
