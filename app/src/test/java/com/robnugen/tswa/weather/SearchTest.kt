package com.robnugen.tswa.weather

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class SearchTest {
    @get:Rule
    val tmp = TemporaryFolder()

    private fun resource(name: String) = checkNotNull(javaClass.classLoader?.getResource(name)).readText()

    private val rochester = resource("geocoding-rochester.json")
    private val forecast = resource("open-meteo-adelaide.json")

    private fun controller(geocode: suspend (String) -> String) =
        WeatherController(WeatherCache(tmp.root.resolve("w.json")), fetch = { forecast }, geocode = geocode)

    @Test
    fun readsTheFirstMatch() {
        val place = Geocoding.firstPlace(rochester)!!
        assertEquals("Rochester, United States", place.fullName)
        assertEquals("Rochester", place.shortName)
    }

    @Test
    fun noResultsMeansNoPlace() {
        assertNull(Geocoding.firstPlace("""{"generationtime_ms":0.15}"""))
    }

    @Test
    fun aFoundPlaceBecomesCurrentAndLoads() =
        runBlocking {
            val c = controller { rochester }
            c.search("  Rochester ")
            assertEquals("Rochester, United States", c.currentPlace.fullName)
            assertEquals("Rochester, United States", c.weather!!.place.fullName)
            assertEquals(WeatherStatus.Fresh, c.status)
            assertNull(c.searchNote)
        }

    @Test
    fun nothingFoundSaysSoAndKeepsThePlace() =
        runBlocking {
            val c = controller { """{"generationtime_ms":0.15}""" }
            c.search("Qzxqvwplk")
            assertEquals(SearchNote.problem("No place found for “Qzxqvwplk”."), c.searchNote)
            assertEquals(Place.ADELAIDE, c.currentPlace)
            c.clearSearch()
            assertNull(c.searchNote)
        }

    @Test
    fun noNetworkSaysSo() =
        runBlocking {
            val c = controller { throw IOException("airplane mode") }
            c.search("Rochester")
            assertEquals(SearchNote.problem("Search failed (offline?)."), c.searchNote)
        }

    @Test
    fun anEmptyQueryDoesNothing() =
        runBlocking {
            val c = controller { error("should not be asked") }
            c.search("   ")
            assertNull(c.searchNote)
        }

    @Test
    fun theQueryIsEncoded() {
        assertEquals(
            "https://geocoding-api.open-meteo.com/v1/search?count=1&language=en&name=New+York",
            OpenMeteo.geocodingUrl("New York"),
        )
    }
}
