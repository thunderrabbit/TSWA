package com.robnugen.tswa.weather

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class WeatherTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private val sample: String =
        checkNotNull(javaClass.classLoader?.getResource("open-meteo-adelaide.json")).readText()

    @Test
    fun parsesTheSampleResponse() {
        val forecast = Forecast.parse(sample)
        assertEquals("Australia/Adelaide", forecast.timezone)
        assertEquals(18.8, forecast.current.temperatureC, 1e-9)
        assertEquals(17.3, forecast.current.feelsLikeC!!, 1e-9)
        assertEquals(2, forecast.current.weatherCode)
        assertEquals(1, forecast.current.isDay)
        assertEquals(240, forecast.hourly.time.size)
        assertEquals(240, forecast.hourly.temperatureC.size)
        assertEquals(10, forecast.daily.time.size)
        assertEquals("2026-11-23T05:57", forecast.daily.sunrise.first())
    }

    @Test
    fun asksForTheWebVersionsFields() {
        val url = OpenMeteo.forecastUrl(Place.ADELAIDE)
        assertTrue(url.startsWith("https://api.open-meteo.com/v1/forecast?latitude=-34.9285&longitude=138.6007"))
        listOf(
            "apparent_temperature",
            "dew_point_2m",
            "pressure_msl",
            "wind_direction_10m",
            "is_day",
            "precipitation_probability,visibility,is_day",
            "uv_index_max",
            "precipitation_probability_max",
            "forecast_days=10",
            "timezone=auto",
        ).forEach { assertTrue(it, url.contains(it)) }
    }

    @Test
    fun describesWeatherCodes() {
        assertEquals("Clear sky", Wmo.describe(0))
        assertEquals("Partly cloudy", Wmo.describe(2))
        assertEquals("Thunderstorm with hail", Wmo.describe(99))
        assertEquals("—", Wmo.describe(42))
        assertEquals("—", Wmo.describe(null))
    }

    @Test
    fun cacheRoundTripsAndToleratesGarbage() {
        val file = tmp.newFile("weather.json")
        val cache = WeatherCache(file)
        val saved = CachedWeather(Place.ADELAIDE, Forecast.parse(sample), fetchedAtMillis = 1_000)
        cache.save(saved)
        assertEquals(saved, cache.load())

        file.writeText("not json")
        assertNull(cache.load())
        assertNull(WeatherCache(tmp.root.resolve("missing.json")).load())
    }

    @Test
    fun freshFetchIsShownAndCached() =
        runBlocking {
            val cache = WeatherCache(tmp.root.resolve("w.json"))
            val controller = WeatherController(cache, fetch = { sample }, clock = { 42 })
            assertNull(controller.weather)
            controller.load(Place.ADELAIDE)
            assertEquals(WeatherStatus.Fresh, controller.status)
            assertEquals(
                18.8,
                controller.weather!!
                    .forecast.current.temperatureC,
                1e-9,
            )
            assertEquals(42L, cache.load()!!.fetchedAtMillis)
        }

    @Test
    fun offlineKeepsTheLastGoodWeather() =
        runBlocking {
            val cache = WeatherCache(tmp.root.resolve("w.json"))
            cache.save(CachedWeather(Place.ADELAIDE, Forecast.parse(sample), fetchedAtMillis = 7))
            val controller = WeatherController(cache, fetch = { throw IOException("airplane mode") })
            controller.load(Place.ADELAIDE)
            assertEquals(WeatherStatus.Offline, controller.status)
            assertEquals(7L, controller.weather!!.fetchedAtMillis)
        }

    @Test
    fun garbledReplyCountsAsOffline() =
        runBlocking {
            val controller = WeatherController(WeatherCache(tmp.root.resolve("w.json")), fetch = { "<html>" })
            controller.load(Place.ADELAIDE)
            assertEquals(WeatherStatus.Offline, controller.status)
            assertNull(controller.weather)
        }

    @Test
    fun comingBackRefetchesOnlyStaleWeather() =
        runBlocking {
            var now = 1_000_000L
            var fetches = 0
            val c =
                WeatherController(
                    WeatherCache(tmp.root.resolve("stale.json")),
                    fetch = {
                        fetches++
                        sample
                    },
                    clock = { now },
                )
            c.load(Place.ADELAIDE)
            assertEquals(1, fetches)
            now += 60_000L // back after a minute
            c.refreshIfStale()
            assertEquals(1, fetches)
            now += WeatherController.STALE_AFTER_MILLIS // back after a long while
            c.refreshIfStale()
            assertEquals(2, fetches)
            assertEquals(now, c.weather!!.fetchedAtMillis)
        }

    @Test
    fun offlineComingBackTriesOnceAndKeepsTheOldData() =
        runBlocking {
            var now = 1_000_000L
            var online = true
            var fetches = 0
            val c =
                WeatherController(
                    WeatherCache(tmp.root.resolve("off.json")),
                    fetch = {
                        fetches++
                        if (!online) throw IOException("airplane mode")
                        sample
                    },
                    clock = { now },
                )
            c.load(Place.ADELAIDE)
            online = false
            now += WeatherController.STALE_AFTER_MILLIS
            c.refreshIfStale()
            assertEquals(2, fetches)
            assertEquals(WeatherStatus.Offline, c.status)
            assertEquals(1_000_000L, c.weather!!.fetchedAtMillis)
        }
}
