package com.robnugen.tswa.temperature

import org.junit.Assert.assertEquals
import org.junit.Test

class VerdictsTest {
    private fun title(celsius: Double) = Verdicts.forCelsius(celsius).title

    @Test
    fun landmarkHitExactlyWinsOverItsBand() {
        assertEquals("Body temp", title(37.0))
        assertEquals("Healthy chicken", title(41.5))
        assertEquals("Absolute zero", title(Scale.ABSOLUTE_ZERO_C))
        assertEquals("Freezing point", title(0.0))
    }

    @Test
    fun aColdLandmarkReadsTheSameOnItAndJustColder() {
        // Not absolute zero (nothing is colder) or freezing (a point: "Exactly here").
        val ranged = Verdicts.landmarks.filter { it.celsius in -273.0..56.7 && it.detail == null && it.celsius != 0.0 }
        ranged.forEach {
            assertEquals(it.label, Verdicts.forCelsius(it.celsius), Verdicts.forCelsius(it.celsius - 0.2))
        }
        assertEquals(Verdicts.forCelsius(-196.0), Verdicts.forCelsius(-195.8))
    }

    @Test
    fun otherwiseTheFirstBandAtOrAboveTheValue() {
        assertEquals("Warm", title(29.3))
        assertEquals("Mild", title(17.4))
        assertEquals("Mild", title(22.0))
        assertEquals("Pleasant", title(22.1))
        assertEquals("Below freezing", title(-5.0))
        assertEquals("Dangerously cold", title(-39.95))
        assertEquals("Hotter than any weather on Earth", title(69.9))
    }

    @Test
    fun betweenTheHottestWeatherAndBoilingWaterBoilsHigherUp() {
        assertEquals("Hotter than any weather on Earth", title(60.0))
        assertEquals("Water boils on top of Everest", title(75.0))
        assertEquals("Water boils on Mount Fuji", title(90.0))
        assertEquals("Water boils in Denver", title(99.9))
    }

    @Test
    fun aHotLandmarkReadsTheSameOnItAndJustPastIt() {
        Verdicts.landmarks.filter { it.celsius > 56.7 }.forEach {
            assertEquals(it.label, Verdicts.forCelsius(it.celsius), Verdicts.forCelsius(it.celsius + 0.2))
        }
        assertEquals(Verdicts.forCelsius(70.2), Verdicts.forCelsius(70.0))
        assertEquals("Water boils at sea level", title(100.0))
    }

    @Test
    fun boilingAtSeaLevelStaysAWhile() {
        assertEquals("Water boils at sea level", title(100.2))
        assertEquals("Water boils at sea level", title(101.0))
        assertEquals("Water boils at the Dead Sea", title(110.0))
        assertEquals("Pressure cooker", title(150.0))
        assertEquals("A moderate oven", title(200.0))
        assertEquals("Pizza oven", title(327.4))
    }

    @Test
    fun aHotValueKeepsTheHottestMilestonePassed() {
        assertEquals("Lead melts", title(400.0))
        assertEquals("Zinc melts", title(450.0))
        assertEquals("Gold melts", title(1_100.0))
        assertEquals("Boron melts", title(2_100.0))
        assertEquals("Tungsten melts", title(5_000.0))
        assertEquals("Surface of the Sun", title(6_000.0))
        assertEquals("The core of the Sun", title(20_000_000.0))
    }

    @Test
    fun aColdValueShowsTheNextMilestoneUp() {
        assertEquals("The afterglow of the Big Bang", title(-272.0))
        assertEquals("Hydrogen boils", title(-260.0))
        assertEquals("Liquid nitrogen boils", title(-200.0))
        assertEquals("Oxygen liquefies", title(-190.0))
        assertEquals("Mercury freezes", title(-39.0))
    }

    @Test
    fun landmarksAreInOrderAndBandsLineUp() {
        val temps = Verdicts.landmarks.map { it.celsius }
        assertEquals(temps.sorted(), temps)
        assertEquals("Healthy chicken", title(41.5))
        assertEquals("Boron melts", title(2_076.0))
        assertEquals("Paper burns", title(232.78))
    }

    @Test
    fun nearALandmarkButNotOnItUsesTheBand() {
        assertEquals("Hot", title(37.01))
    }

    @Test
    fun everyPresetIsInsidePhysics() {
        Verdicts.presets.forEach { assert(it.celsius >= Scale.ABSOLUTE_ZERO_C) }
        assertEquals(10, Verdicts.presets.size)
        assertEquals(45, Verdicts.landmarks.size)
    }
}
