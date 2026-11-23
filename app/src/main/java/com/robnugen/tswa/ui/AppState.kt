package com.robnugen.tswa.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.robnugen.tswa.settings.TswaPrefs
import com.robnugen.tswa.temperature.Scale
import com.robnugen.tswa.temperature.ScaleChoice
import com.robnugen.tswa.weather.WeatherController

// App-wide state that outlives a page: the scale shown everywhere, the
// scales the tap visits, the converter's value, the saved places and the
// weather. Every change is saved straight away.
class AppState(
    private val prefs: TswaPrefs,
    val weather: WeatherController,
) {
    var enabledScales by mutableStateOf(prefs.enabledScales)
        private set

    // The tab on screen; the app reopens on it.
    var tab by mutableStateOf(Tab.entries.firstOrNull { it.name == prefs.lastTab } ?: Tab.Weather)
        private set

    fun showTab(next: Tab) {
        tab = next
        prefs.lastTab = next.name
    }

    var scale by mutableStateOf(ScaleChoice.shown(prefs.shownScale, prefs.enabledScales))
        private set

    // The tap on any temperature: next scale that is on.
    fun stepScale() {
        scale = Scale.next(scale, enabledScales)
        prefs.shownScale = scale
    }

    // The converter's value. It follows the weather's current temperature
    // until the person sets it by hand (slider or preset), as on the web.
    var converterC by mutableDoubleStateOf(DEFAULT_CONVERTER_C)
        private set
    private var converterTouched = false

    fun setConverter(celsius: Double) {
        converterTouched = true
        converterC = celsius
    }

    fun seedConverter(celsius: Double) {
        if (!converterTouched) converterC = celsius
    }

    // The saved places and what can be done to them.
    val places = PlacesState(prefs)

    // A Settings switch. If the shown scale goes off, show the first one on.
    fun toggleScale(target: Scale) {
        enabledScales = ScaleChoice.toggle(enabledScales, target)
        prefs.enabledScales = enabledScales
        scale = ScaleChoice.shown(scale, enabledScales)
        prefs.shownScale = scale
    }
}

// Where the converter starts when there is no weather to seed it.
private const val DEFAULT_CONVERTER_C = 22.2
