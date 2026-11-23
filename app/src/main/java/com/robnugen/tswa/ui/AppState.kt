package com.robnugen.tswa.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.robnugen.tswa.settings.TswaPrefs
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.temperature.ScaleChoice

// App-wide state that outlives a page: the scale shown everywhere and
// the scales the tap visits. Every change is saved straight away.
class AppState(
    private val prefs: TswaPrefs,
) {
    var enabledScales by mutableStateOf(prefs.enabledScales)
        private set

    var scale by mutableStateOf(ScaleChoice.shown(prefs.shownScale, prefs.enabledScales))
        private set

    // The tap on any temperature: next scale that is on.
    fun stepScale() {
        scale = Scale.next(scale, enabledScales)
        prefs.shownScale = scale
    }

    // A Settings switch. If the shown scale goes off, show the first one on.
    fun toggleScale(target: Scale) {
        enabledScales = ScaleChoice.toggle(enabledScales, target)
        prefs.enabledScales = enabledScales
        scale = ScaleChoice.shown(scale, enabledScales)
        prefs.shownScale = scale
    }
}
