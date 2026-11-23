package com.robnugen.tswa.temperature

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScaleChoiceTest {
    private val cf = setOf(Scale.C, Scale.F)

    @Test
    fun switchesAScaleOnAndOff() {
        assertEquals(setOf(Scale.C, Scale.F, Scale.K), ScaleChoice.toggle(cf, Scale.K))
        assertEquals(setOf(Scale.C), ScaleChoice.toggle(cf, Scale.F))
    }

    @Test
    fun refusesToSwitchOffTheLastScale() {
        assertEquals(setOf(Scale.R), ScaleChoice.toggle(setOf(Scale.R), Scale.R))
        assertFalse(ScaleChoice.canToggle(setOf(Scale.R), Scale.R))
        assertTrue(ScaleChoice.canToggle(setOf(Scale.R), Scale.C))
        assertTrue(ScaleChoice.canToggle(cf, Scale.C))
    }

    @Test
    fun keepsTheShownScaleWhileItIsOn() {
        assertEquals(Scale.F, ScaleChoice.shown(Scale.F, cf))
    }

    @Test
    fun fallsBackToTheFirstScaleThatIsOn() {
        assertEquals(Scale.C, ScaleChoice.shown(Scale.F, setOf(Scale.C, Scale.K)))
        assertEquals(Scale.K, ScaleChoice.shown(Scale.C, setOf(Scale.R, Scale.K)))
    }
}
