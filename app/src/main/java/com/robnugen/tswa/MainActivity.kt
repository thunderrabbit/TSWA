package com.robnugen.tswa

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.robnugen.tswa.settings.TswaPrefs
import com.robnugen.tswa.ui.AppState
import com.robnugen.tswa.ui.TswaApp
import com.robnugen.tswa.ui.TswaTheme
import com.robnugen.tswa.weather.WeatherCache
import com.robnugen.tswa.weather.WeatherController
import java.io.File

private const val WEATHER_CACHE_FILE = "weather_cache.json"
private const val LOG_TAG = "TSWA"

// TSWA's native UI (Jetpack Compose; see the README for why). Rob's
// saved places from the old WebView version are still in the app's
// data directory, untouched, until the saved-places work migrates them.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val weather =
            WeatherController(
                WeatherCache(File(filesDir, WEATHER_CACHE_FILE)),
                onFailure = { Log.w(LOG_TAG, "Weather fetch failed", it) },
            )
        val state = AppState(TswaPrefs(this), weather)
        setContent {
            TswaTheme {
                TswaApp(state)
            }
        }
    }
}
