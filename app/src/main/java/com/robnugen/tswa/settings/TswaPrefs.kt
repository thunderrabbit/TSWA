package com.robnugen.tswa.settings

import android.content.Context
import com.robnugen.tswa.places.SavedPlaces
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.weather.Place

// Every setting TSWA keeps between runs, in one SharedPreferences file.
class TswaPrefs(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    // Which scales the tap visits; the defaults when nothing usable is saved.
    var enabledScales: Set<Scale>
        get() =
            prefs
                .getStringSet(KEY_ENABLED_SCALES, null)
                ?.mapNotNull(::scaleNamed)
                ?.toSet()
                ?.takeIf { it.isNotEmpty() }
                ?: Scale.DEFAULT_ENABLED
        set(value) {
            prefs.edit().putStringSet(KEY_ENABLED_SCALES, value.map { it.name }.toSet()).apply()
        }

    // The scale currently shown, everywhere in the app.
    var shownScale: Scale
        get() = prefs.getString(KEY_SHOWN_SCALE, null)?.let(::scaleNamed) ?: Scale.C
        set(value) {
            prefs.edit().putString(KEY_SHOWN_SCALE, value.name).apply()
        }

    // The tab the app was last on, by name; it reopens there.
    var lastTab: String?
        get() = prefs.getString(KEY_LAST_TAB, null)
        set(value) {
            prefs.edit().putString(KEY_LAST_TAB, value).apply()
        }

    // The saved places, in display order.
    var savedPlaces: List<Place>
        get() = prefs.getString(KEY_SAVED_PLACES, null)?.let(SavedPlaces::decode).orEmpty()
        set(value) {
            prefs.edit().putString(KEY_SAVED_PLACES, SavedPlaces.encode(value)).apply()
        }

    // False until the first-run location question has been asked, and
    // false again once every saved place is deleted (that is how the
    // person gets back into onboarding). Null when never recorded.
    var onboarded: Boolean?
        get() = if (prefs.contains(KEY_ONBOARDED)) prefs.getBoolean(KEY_ONBOARDED, false) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove(KEY_ONBOARDED).apply()
            } else {
                prefs.edit().putBoolean(KEY_ONBOARDED, value).apply()
            }
        }

    private fun scaleNamed(name: String): Scale? = Scale.entries.firstOrNull { it.name == name }

    private companion object {
        const val FILE = "tswa_settings"
        const val KEY_ENABLED_SCALES = "enabled_scales"
        const val KEY_SHOWN_SCALE = "shown_scale"
        const val KEY_SAVED_PLACES = "saved_places"
        const val KEY_ONBOARDED = "onboarded"
        const val KEY_LAST_TAB = "last_tab"
    }
}
