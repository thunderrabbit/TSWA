package com.robnugen.tswa.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.robnugen.tswa.places.Onboarding
import com.robnugen.tswa.places.SavedPlaces
import com.robnugen.tswa.settings.TswaPrefs
import com.robnugen.tswa.weather.Place

// The saved places, in display order, and everything done to them. Every
// change is saved straight away, so the Weather tab's chips follow at once.
class PlacesState(
    private val prefs: TswaPrefs,
) {
    var saved by mutableStateOf(prefs.savedPlaces)
        private set

    // The last place swiped away and where it was, for Undo.
    var lastDeleted by mutableStateOf<Pair<Int, Place>?>(null)
        private set

    private fun keep(places: List<Place>) {
        saved = places
        prefs.savedPlaces = places
    }

    // The Save button: the place on screen goes to the top of the list.
    fun save(place: Place) = keep(SavedPlaces.saveOnTop(saved, place))

    // A drop at the end of a drag.
    fun move(
        from: Int,
        to: Int,
    ) = keep(SavedPlaces.move(saved, from, to))

    // Swipe right to delete. Emptying the list sends the person back
    // through onboarding on the next launch.
    fun delete(place: Place) {
        val index = saved.indexOfFirst { SavedPlaces.same(it, place) }
        if (index >= 0) {
            keep(saved.filterIndexed { i, _ -> i != index })
            if (saved.isEmpty()) prefs.onboarded = false
            lastDeleted = index to place
        }
    }

    fun undoDelete() {
        val (index, place) = lastDeleted ?: return
        keep(SavedPlaces.restore(saved, place, index))
        if (prefs.onboarded == false) prefs.onboarded = true
        lastDeleted = null
    }

    fun forgetDeleted() {
        lastDeleted = null
    }

    // Whether to ask about location at this launch (mg #899).
    fun onboardingNeeded(): Boolean = Onboarding.needed(prefs.onboarded, saved.isEmpty())

    fun markOnboarded() {
        prefs.onboarded = true
    }
}
