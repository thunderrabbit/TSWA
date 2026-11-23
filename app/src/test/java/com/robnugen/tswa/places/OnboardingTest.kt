package com.robnugen.tswa.places

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingTest {
    @Test
    fun aFreshInstallIsAsked() {
        assertTrue(Onboarding.needed(onboarded = null, savedIsEmpty = true))
    }

    @Test
    fun someoneWithPlacesFromTheOldVersionIsNotAsked() {
        assertFalse(Onboarding.needed(onboarded = null, savedIsEmpty = false))
    }

    @Test
    fun emptyingTheListAsksAgainOnce() {
        assertTrue(Onboarding.needed(onboarded = false, savedIsEmpty = true))
        assertFalse(Onboarding.needed(onboarded = true, savedIsEmpty = true))
    }

    @Test
    fun namesTheLocationFromTheTimezone() {
        val place = Onboarding.placeFromTimezone("Australia/Adelaide", -34.9, 138.6)
        assertEquals("Your location near Adelaide", place.fullName)
        assertEquals("Adelaide", place.shortName)
        assertEquals("New York", Onboarding.placeFromTimezone("America/New_York", 0.0, 0.0).shortName)
        assertEquals("Indianapolis", Onboarding.placeFromTimezone("America/Indiana/Indianapolis", 0.0, 0.0).shortName)
        assertEquals("here", Onboarding.placeFromTimezone("", 0.0, 0.0).shortName)
    }
}
