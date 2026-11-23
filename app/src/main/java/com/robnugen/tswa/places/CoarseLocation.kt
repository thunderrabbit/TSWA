package com.robnugen.tswa.places

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// One coarse location fix for onboarding: the network provider's last
// known fix if there is one, otherwise a fresh one. Null on any failure
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
        val provider = LocationManager.NETWORK_PROVIDER
        val usable = permitted() && manager != null && manager.isProviderEnabled(provider)
        return if (!usable) {
            null
        } else {
            manager.getLastKnownLocation(provider) ?: withTimeoutOrNull(TIMEOUT_MS) {
                suspendCancellableCoroutine { answer ->
                    val cancel = CancellationSignal()
                    answer.invokeOnCancellation { cancel.cancel() }
                    LocationManagerCompat.getCurrentLocation(manager, provider, cancel, context.mainExecutor) {
                        if (answer.isActive) answer.resume(it)
                    }
                }
            }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 10_000L
    }
}
