package com.robnugen.tswa

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient

// TSWA v0: a thin native shell around the TSWA web UI (assets/www).
// The point of this phase is UI iteration on a real phone: the HTML/CSS/JS
// is the exact interaction Rob approved (tap the number to step the scale,
// centre-pinned slider below it), and this shell just makes it an app.
class MainActivity : Activity() {
    private companion object {
        const val LOCATION_REQUEST_CODE = 42
    }

    private lateinit var webView: WebView

    // A geolocation prompt waiting on the person's permission decision.
    private var pendingOrigin: String? = null
    private var pendingCallback: GeolocationPermissions.Callback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            setGeolocationEnabled(true)
            loadWithOverviewMode = true
            useWideViewPort = true
        }
        webView.webViewClient = WebViewClient()
        webView.webChromeClient =
            object : WebChromeClient() {
                override fun onGeolocationPermissionsShowPrompt(
                    origin: String?,
                    callback: GeolocationPermissions.Callback?,
                ) {
                    val granted =
                        checkSelfPermission(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ) == PackageManager.PERMISSION_GRANTED
                    if (granted) {
                        callback?.invoke(origin, true, false)
                    } else {
                        // Answer only once the person has decided. The
                        // page is waiting on this callback, so replying
                        // now would refuse a location the user is about
                        // to allow (TSWA onboarding, mg #883).
                        pendingOrigin = origin
                        pendingCallback = callback
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            LOCATION_REQUEST_CODE,
                        )
                    }
                }
            }
        webView.loadUrl("file:///android_asset/www/index.html")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != LOCATION_REQUEST_CODE) return
        val callback = pendingCallback ?: return
        val granted = grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        callback.invoke(pendingOrigin, granted, false)
        pendingOrigin = null
        pendingCallback = null
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
