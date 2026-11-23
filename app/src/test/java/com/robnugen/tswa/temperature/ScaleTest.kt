package com.robnugen.tswa.temperature

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

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
        assertEquals("273.2K", Scale.K.format(0.0))
        assertEquals("491.7°R", Scale.R.format(0.0))
        assertEquals("63.3", Scale.F.format(17.4, withSymbol = false))
    }

    @Test
    fun roundsExactHalvesAwayFromZero() {
        // -273.15 is stored as -273.1499999..., which the web showed as -273.1.
        assertEquals("-273.2°C", Scale.C.format(Scale.ABSOLUTE_ZERO_C))
        assertEquals("-459.7°F", Scale.F.format(Scale.ABSOLUTE_ZERO_C))
        assertEquals("0.0°R", Scale.R.format(Scale.ABSOLUTE_ZERO_C))
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
    fun belowFiveThousandKeepsOneDecimal() {
        assertEquals("3422.0°C", Scale.C.format(3_422.0))
        assertEquals("4228.5°R", Scale.R.format(2_076.0))
        assertEquals("4999.9°C", Scale.C.format(4_999.9))
    }

    @Test
    fun fromFiveThousandShowsThreeFiguresWithCommas() {
        assertEquals("5,000°C", Scale.C.format(5_000.0))
        assertEquals("5,000°C", Scale.C.format(4_999.96))
        assertEquals("5,500°C", Scale.C.format(5_498.85))
        assertEquals("9,930°F", Scale.F.format(5_498.85))
        assertEquals("5,770K", Scale.K.format(5_498.85))
        assertEquals("10,400°R", Scale.R.format(5_498.85))
        assertEquals("36,500°R", Scale.R.format(20_000.0))
        assertEquals("180,000°R", Scale.R.format(99_726.85))
        assertEquals("999,000°C", Scale.C.format(999_499.0))
        assertEquals("20,000", Scale.C.format(20_000.0, withSymbol = false))
    }

    @Test
    fun fromAMillionReadsInWords() {
        assertEquals("1 million °C", Scale.C.format(999_999.0))
        assertEquals("1 million °C", Scale.C.format(1_000_000.0))
        assertEquals("1.8 million °R", Scale.R.format(1_000_000.0))
        assertEquals("1.23 million °C", Scale.C.format(1_234_000.0))
        assertEquals("27 million °R", Scale.R.format(15_000_000.0))
        assertEquals("100 billion °C", Scale.C.format(100_000_000_000.0))
        assertEquals("10 trillion °C", Scale.C.format(10_000_000_000_000.0))
        assertEquals("10 trillion K", Scale.K.format(10_000_000_000_000.0))
        assertEquals("18 trillion °F", Scale.F.format(10_000_000_000_000.0))
        assertEquals("18 trillion", Scale.R.format(10_000_000_000_000.0, withSymbol = false))
        assertEquals("1,000 quadrillion °C", Scale.C.format(1e18))
    }

    @Test
    fun bigNumbersIgnoreThePhoneLocale() {
        val saved = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("20,000°C", Scale.C.format(20_000.0))
            assertEquals("1.8 million °R", Scale.R.format(1_000_000.0))
        } finally {
            Locale.setDefault(saved)
        }
    }

    @Test
    fun defaultsToCelsiusAndFahrenheit() {
        assertEquals(setOf(Scale.C, Scale.F), Scale.DEFAULT_ENABLED)
    }
}
