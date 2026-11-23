package com.robnugen.tswa.places

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume

// Reads what the WebView version left in localStorage, through a hidden
// WebView and a tiny page in the old app's folder. Read-only.
class LegacyReader(
    private val context: Context,
) {
    // The stored values by key (null where a key was never set), or null
    // when the page did not answer in time, so the caller can try again.
    @SuppressLint("SetJavaScriptEnabled") // our own bundled page, nothing remote
    suspend fun read(): Map<String, String?>? =
        withContext(Dispatchers.Main) {
            val webView = WebView(context)
            try {
                withTimeoutOrNull(TIMEOUT_MS) {
                    suspendCancellableCoroutine { answer ->
                        webView.settings.javaScriptEnabled = true
                        webView.settings.domStorageEnabled = true
                        webView.addJavascriptInterface(
                            object {
                                @JavascriptInterface
                                fun receive(found: String) {
                                    if (answer.isActive) answer.resume(found)
                                }
                            },
                            BRIDGE,
                        )
                        webView.loadUrl(PAGE)
                    }
                }?.let(::decode)
            } finally {
                webView.destroy()
            }
        }

    private fun decode(found: String): Map<String, String?>? =
        try {
            Json.decodeFromString<Map<String, String?>>(found)
        } catch (ignoredGarbled: SerializationException) {
            null // the page answered with something odd: try again next start
        }

    private companion object {
        const val PAGE = "file:///android_asset/www/read-old-storage.html"
        const val BRIDGE = "TswaLegacy"
        const val TIMEOUT_MS = 5_000L
    }
}
