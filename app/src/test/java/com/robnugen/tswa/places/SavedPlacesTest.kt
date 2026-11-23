package com.robnugen.tswa.places

import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.Place
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPlacesTest {
    private val adelaide = Place.ADELAIDE
    private val austin = Place("Austin, United States", "Austin", 30.26715, -97.74306)

    @Test
    fun placesWithinAThousandthOfADegreeAreTheSame() {
        assertTrue(SavedPlaces.same(adelaide, adelaide.copy(latitude = -34.9289, longitude = 138.6003)))
        assertFalse(SavedPlaces.same(adelaide, adelaide.copy(latitude = -34.93)))
    }

    @Test
    fun savingPutsThePlaceOnTopOnce() {
        val list = SavedPlaces.saveOnTop(listOf(adelaide), austin)
        assertEquals(listOf(austin, adelaide), list)
        assertEquals(list, SavedPlaces.saveOnTop(list, adelaide.copy(fullName = "Adelaide again")))
    }

    @Test
    fun theListRoundTrips() {
        val list = listOf(austin, adelaide)
        assertEquals(list, SavedPlaces.decode(SavedPlaces.encode(list)))
        assertEquals(emptyList<Place>(), SavedPlaces.decode("not json"))
    }

    @Test
    fun readsTheOldWebViewPlacesInOrder() {
        val old =
            """[{"label":"Adelaide","lat":-34.9285,"lon":138.6007,"full":"Adelaide, Australia"},""" +
                """{"label":"Yurigaoka","lat":35.6,"lon":139.5,"full":"Yurigaoka, Japan"},""" +
                """{"label":"Home","lat":1.0,"lon":2.0}]"""
        val places = LegacyStorage.places(old)
        assertEquals(listOf("Adelaide", "Yurigaoka", "Home"), places.map { it.shortName })
        assertEquals("Adelaide, Australia", places[0].fullName)
        assertEquals("Home", places[2].fullName)
        assertEquals(emptyList<Place>(), LegacyStorage.places(null))
        assertEquals(emptyList<Place>(), LegacyStorage.places("garbage"))
    }

    @Test
    fun readsTheOldScaleChoice() {
        assertEquals(setOf(Scale.C, Scale.F), LegacyStorage.scales("""{"C":true,"F":true,"K":false,"R":false}"""))
        assertEquals(setOf(Scale.K), LegacyStorage.scales("""{"C":false,"F":false,"K":true}"""))
        assertNull(LegacyStorage.scales("""{"C":false,"F":false}"""))
        assertNull(LegacyStorage.scales(null))
        assertNull(LegacyStorage.scales("nope"))
    }
}
