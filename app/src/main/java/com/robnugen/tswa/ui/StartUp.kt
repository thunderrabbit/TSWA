package com.robnugen.tswa.ui

import android.Manifest
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.robnugen.tswa.places.CoarseLocation
import com.robnugen.tswa.places.Onboarding
import com.robnugen.tswa.weather.Place
import com.robnugen.tswa.weather.WeatherController
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch

// What happens once when the app opens: either ask about location (first
// run, or after the saved list was emptied; mg #899) or refresh the last
// place shown.
@Composable
fun StartUp(state: AppState) {
    val context = LocalContext.current
    val location = remember { CoarseLocation(context) }
    // The permission answer arrives here; start-up waits for it before
    // deciding what to show (the WebView version once did not, mg #883).
    val answer = remember { CompletableDeferred<Boolean>() }
    val ask =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            answer.complete(granted)
        }
    LaunchedEffect(Unit) {
        if (state.places.onboardingNeeded()) {
            val allowed =
                location.permitted() ||
                    run {
                        ask.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        answer.await()
                    }
            val here = if (allowed) location.current() else null
            Log.i("TSWA", "Onboarding: location ${if (allowed) "allowed" else "refused"}, fix ${here != null}")
            if (here == null) {
                state.weather.load(Place.ADELAIDE)
            } else {
                // Not saved: the list stays empty until the person saves a place.
                val probe = Place("Your location", "Here", here.latitude, here.longitude)
                state.weather.load(probe) { forecast ->
                    Onboarding.placeFromTimezone(forecast.timezone, here.latitude, here.longitude)
                }
            }
            state.places.markOnboarded()
        } else {
            // The last place shown: its cached weather is already on
            // screen, and this refreshes it (Adelaide when nothing is cached).
            state.weather.load(state.weather.currentPlace)
        }
    }
}

// Returning to the app (launcher, recents, screen on) reloads the weather
// if it has gone stale; a cold start is handled by StartUp (mg #908).
@Composable
fun RefreshOnReturn(weather: WeatherController) {
    val owner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    DisposableEffect(owner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) scope.launch { weather.refreshIfStale() }
            }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}
