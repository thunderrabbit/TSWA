package com.robnugen.tswa.temperature

import org.junit.Assert.assertEquals
import org.junit.Test

class VerdictsTest {
    private fun title(celsius: Double) = Verdicts.forCelsius(celsius).title

    @Test
    fun landmarkHitExactlyWinsOverItsBand() {
        assertEquals("Boiling - water boils", title(100.0))
        assertEquals("Body temp", title(37.0))
        assertEquals("Freezing - water freezes", title(0.0))
        assertEquals("Paper burns", title(232.78))
        assertEquals("Absolute zero", title(Scale.ABSOLUTE_ZERO_C))
    }

    @Test
    fun otherwiseTheFirstBandAtOrAboveTheValue() {
        assertEquals("Warm", title(29.3))
        assertEquals("Mild", title(17.4))
        assertEquals("Mild", title(22.0))
        assertEquals("Pleasant", title(22.1))
        assertEquals("Below freezing", title(-5.0))
        assertEquals("Dangerously cold", title(-39.95))
        assertEquals("Hotter than any weather on Earth", title(99.9))
    }

    @Test
    fun aboveEveryBandIsOvenTerritory() {
        assertEquals("Oven territory", title(150.0))
        assertEquals("Oven territory", title(1_000.0))
    }

    @Test
    fun nearALandmarkButNotOnItUsesTheBand() {
        assertEquals("Hot", title(37.01))
    }

    @Test
    fun everyPresetIsInsidePhysics() {
        Verdicts.presets.forEach { assert(it.celsius >= Scale.ABSOLUTE_ZERO_C) }
        assertEquals(10, Verdicts.presets.size)
        assertEquals(13, Verdicts.landmarks.size)
    }
}
