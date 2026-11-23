package com.robnugen.tswa.places

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// One coarse location fix for onboarding: a last known fix if there is
// one, otherwise a fresh one. Null on any failure
// or after ten seconds, and the app shows Adelaide instead.
class CoarseLocation(
    private val context: Context,
) {
    fun permitted(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // checked by permitted() first
    suspend fun current(): Location? {
        val manager = context.getSystemService(LocationManager::class.java)
        val providers = if (permitted() && manager != null) usableProviders(manager) else emptyList()
        val known = providers.firstNotNullOfOrNull { manager?.getLastKnownLocation(it) }
        return when {
            providers.isEmpty() -> null
            known != null -> known
            else -> withTimeoutOrNull(TIMEOUT_MS) { freshFix(checkNotNull(manager), providers.first()) }
        }
    }

    // The fused provider (Android 12+) blends whatever the phone has; the
    // network and GPS providers are the fallbacks, in that order.
    private fun usableProviders(manager: LocationManager): List<String> =
        listOfNotNull(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) LocationManager.FUSED_PROVIDER else null,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
        ).filter { manager.isProviderEnabled(it) }

    @SuppressLint("MissingPermission") // only reached after permitted()
    private suspend fun freshFix(
        manager: LocationManager,
        provider: String,
    ): Location? =
        suspendCancellableCoroutine { answer ->
            val cancel = CancellationSignal()
            answer.invokeOnCancellation { cancel.cancel() }
            LocationManagerCompat.getCurrentLocation(manager, provider, cancel, context.mainExecutor) {
                if (answer.isActive) answer.resume(it)
            }
        }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
