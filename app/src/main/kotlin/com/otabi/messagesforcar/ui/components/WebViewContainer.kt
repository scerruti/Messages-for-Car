package com.otabi.messagesforcar.ui.components

import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Helper functions for WebView text injection.
 */
fun WebView.focusInputField() {
    evaluateJavascript(
        "document.querySelector('textarea, input[type=text]').focus();",
        null
    )
}

fun WebView.injectMessageText(text: String) {
    val escaped = text.replace("'", "\\'")
    evaluateJavascript(
        "document.querySelector('textarea, input[type=text]').value = '$escaped';",
        null
    )
}

fun WebView.sendMessage() {
    evaluateJavascript(
        "document.querySelector('[aria-label=\"Send\"]').click();",
        null
    )
}

/**
 * JavaScript interface for detecting pairing state changes.
 */
class PairingStateInterface(private val onPairingStateChanged: ((Boolean) -> Unit)?) {
    @JavascriptInterface
    fun onPairingDetected(isPaired: Boolean) {
        onPairingStateChanged?.invoke(isPaired)
    }
}

/**
 * Basic WebView container for Messages for Web with pairing detection.
 * Embeds WebView in Compose with automotive-safe configuration and pairing state monitoring.
 */
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    onPairingStateChanged: ((Boolean) -> Unit)? = null,
    injectText: String? = null,
    onTextInjected: (() -> Unit)? = null,
    autoSend: Boolean = false
) {
    var webView by remember { mutableStateOf<WebView?>(null) }

    // Inject text when requested
    LaunchedEffect(injectText) {
        val text = injectText ?: return@LaunchedEffect
        webView?.apply {
            focusInputField()
            injectMessageText(text)
            if (autoSend) {
                sendMessage()
            }
        }
        onTextInjected?.invoke()
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webView = this

                // Add JavaScript interface for pairing detection
                addJavascriptInterface(
                    PairingStateInterface(onPairingStateChanged),
                    "AndroidPairing"
                )

                // Automotive-optimized WebView settings
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    @Suppress("DEPRECATION")
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                    cacheMode = WebSettings.LOAD_DEFAULT
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                // Restrict navigation to Google Messages domain for security
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return if (url?.contains("messages.google.com") == true ||
                            url?.contains("accounts.google.com") == true
                        ) {
                            false // Allow
                        } else {
                            true // Block external navigation
                        }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)

                        // Inject pairing detection script
                        view?.evaluateJavascript(
                            """
                            (function() {
                                console.log('Injecting pairing detection');
                                
                                function checkPairingState() {
                                    const isPaired =
                                        document.querySelector('[data-testid="conversation-list"]') !== null ||
                                        document.querySelector('.conversation-list') !== null ||
                                        document.querySelector('[role="main"]') !== null;
                                    
                                    if (isPaired) {
                                        console.log('Pairing detected');
                                        AndroidPairing.onPairingDetected(true);
                                    }
                                }
                                
                                checkPairingState();
                                setInterval(checkPairingState, 2000);
                            })();
                            """.trimIndent(),
                            null
                        )
                    }
                }

                // Load Messages for Web
                loadUrl("https://messages.google.com/web")
            }
        },
        update = { view ->
            webView = view
        })
}
