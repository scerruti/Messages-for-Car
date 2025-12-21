package com.otabi.messagesforcar.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Basic WebView container for Messages for Web.
 * Embeds WebView in Compose with automotive-safe configuration.
 */
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Automotive-optimized WebView settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                // Restrict navigation to Google Messages domain for security
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return if (url?.contains("messages.google.com") == true ||
                                   url?.contains("accounts.google.com") == true) {
                            false // Allow
                        } else {
                            true // Block external navigation
                        }
                    }
                }

                // Load Messages for Web
                loadUrl("https://messages.google.com/web")
            }
        }
    )
}