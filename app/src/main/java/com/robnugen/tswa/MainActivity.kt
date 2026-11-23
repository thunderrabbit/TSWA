package com.robnugen.tswa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.robnugen.tswa.settings.TswaPrefs
import com.robnugen.tswa.ui.AppState
import com.robnugen.tswa.ui.TswaApp
import com.robnugen.tswa.ui.TswaTheme

// TSWA's native UI (Jetpack Compose; see the README for why). Rob's
// saved places from the old WebView version are still in the app's
// data directory, untouched, until the saved-places work migrates them.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val state = AppState(TswaPrefs(this))
        setContent {
            TswaTheme {
                TswaApp(state)
            }
        }
    }
}
