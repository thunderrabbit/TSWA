package com.robnugen.tswa.settings

import android.content.Context
import com.robnugen.tswa.temperature.Scale

// Every setting TSWA keeps between runs, in one SharedPreferences file.
// (The old WebView version's localStorage lives elsewhere in the app's
// data directory and is not touched here.)
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

    private fun scaleNamed(name: String): Scale? = Scale.entries.firstOrNull { it.name == name }

    private companion object {
        const val FILE = "tswa_settings"
        const val KEY_ENABLED_SCALES = "enabled_scales"
        const val KEY_SHOWN_SCALE = "shown_scale"
    }
}
