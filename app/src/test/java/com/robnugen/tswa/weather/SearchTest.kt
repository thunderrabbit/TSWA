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
    private val portland = resource("geocoding-portland.json")
    private val forecast = resource("open-meteo-adelaide.json")

    private fun controller(geocode: suspend (String) -> String) =
        WeatherController(WeatherCache(tmp.root.resolve("w.json")), fetch = { forecast }, geocode = geocode)

    @Test
    fun readsAMatchWithItsState() {
        val match = Geocoding.matches(rochester).single()
        assertEquals("Rochester, New York, United States", match.place.fullName)
        assertEquals("Rochester", match.place.shortName)
        assertEquals("Monroe, New York, United States", match.area)
    }

    @Test
    fun readsEveryMatchInOrder() {
        val matches = Geocoding.matches(portland)
        assertEquals(10, matches.size)
        assertEquals("Portland, Oregon, United States", matches[0].place.fullName)
        assertEquals("Portland, Maine, United States", matches[1].place.fullName)
    }

    @Test
    fun leavesOutMissingAndRepeatedParts() {
        val reply = """{"results":[{"name":"Tokyo","admin1":"Tokyo","country":"Japan","latitude":35.7,"longitude":139.7}]}"""
        val match = Geocoding.matches(reply).single()
        assertEquals("Tokyo, Japan", match.place.fullName)
        assertEquals("Tokyo, Japan", match.area)
    }

    @Test
    fun noResultsMeansNoMatches() {
        assertEquals(emptyList<SearchMatch>(), Geocoding.matches("""{"generationtime_ms":0.15}"""))
    }

    @Test
    fun aFoundPlaceBecomesCurrentAndLoads() =
        runBlocking {
            val c = controller { rochester }
            c.search("  Rochester ")
            assertEquals("Rochester, New York, United States", c.currentPlace.fullName)
            assertEquals("Rochester, New York, United States", c.weather!!.place.fullName)
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
            "https://geocoding-api.open-meteo.com/v1/search?count=10&language=en&name=New+York",
            OpenMeteo.geocodingUrl("New York"),
        )
    }
}
