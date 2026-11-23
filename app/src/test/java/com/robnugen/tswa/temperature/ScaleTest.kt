package com.robnugen.tswa.temperature

import org.junit.Assert.assertEquals
import org.junit.Test

class ScaleTest {
    private val eps = 1e-9

    @Test
    fun convertsFromCelsius() {
        assertEquals(100.0, Scale.C.convert(100.0), eps)
        assertEquals(212.0, Scale.F.convert(100.0), eps)
        assertEquals(32.0, Scale.F.convert(0.0), eps)
        assertEquals(-40.0, Scale.F.convert(-40.0), eps)
        assertEquals(273.15, Scale.K.convert(0.0), eps)
        assertEquals(491.67, Scale.R.convert(0.0), eps)
        assertEquals(0.0, Scale.K.convert(Scale.ABSOLUTE_ZERO_C), eps)
        assertEquals(0.0, Scale.R.convert(Scale.ABSOLUTE_ZERO_C), eps)
    }

    @Test
    fun formatsToOneDecimalWithSymbol() {
        assertEquals("22.2°C", Scale.C.format(22.2))
        assertEquals("212.0°F", Scale.F.format(100.0))
        assertEquals("273.1K", Scale.K.format(0.0))
        assertEquals("491.7°R", Scale.R.format(0.0))
        assertEquals("63.3", Scale.F.format(17.4, withSymbol = false))
    }

    @Test
    fun roundsLikeTheWebVersion() {
        // -273.15 is stored as -273.1499999..., which the web showed as -273.1.
        assertEquals("-273.1°C", Scale.C.format(Scale.ABSOLUTE_ZERO_C))
        assertEquals("232.8°C", Scale.C.format(232.78))
        assertEquals("451.0°F", Scale.F.format(232.78))
    }

    @Test
    fun neverShowsNegativeZero() {
        assertEquals("0.0°C", Scale.C.format(-0.04))
        assertEquals("0.0K", Scale.K.format(Scale.ABSOLUTE_ZERO_C))
    }

    @Test
    fun stepsThroughEnabledScalesInOrderAndWraps() {
        val cf = setOf(Scale.C, Scale.F)
        assertEquals(Scale.F, Scale.next(Scale.C, cf))
        assertEquals(Scale.C, Scale.next(Scale.F, cf))

        val all = Scale.entries.toSet()
        assertEquals(Scale.F, Scale.next(Scale.C, all))
        assertEquals(Scale.K, Scale.next(Scale.F, all))
        assertEquals(Scale.R, Scale.next(Scale.K, all))
        assertEquals(Scale.C, Scale.next(Scale.R, all))

        val cr = setOf(Scale.C, Scale.R)
        assertEquals(Scale.R, Scale.next(Scale.C, cr))
        assertEquals(Scale.C, Scale.next(Scale.R, cr))
    }

    @Test
    fun staysPutWithOneScaleEnabled() {
        assertEquals(Scale.K, Scale.next(Scale.K, setOf(Scale.K)))
    }

    @Test
    fun leavesAScaleThatIsNoLongerEnabled() {
        assertEquals(Scale.R, Scale.next(Scale.F, setOf(Scale.C, Scale.R)))
        assertEquals(Scale.C, Scale.next(Scale.F, setOf(Scale.C)))
    }

    @Test
    fun defaultsToCelsiusAndFahrenheit() {
        assertEquals(setOf(Scale.C, Scale.F), Scale.DEFAULT_ENABLED)
    }
}
