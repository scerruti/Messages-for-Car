import com.otabi.messagesforcar.pairing.PairingState
import com.otabi.messagesforcar.ui.components.PairingStateInterface

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
 * Basic WebView container for Messages for Web with pairing detection.
 * Embeds WebView in Compose with automotive-safe configuration and pairing state monitoring.
 */
@Composable
fun WebViewContainer(
    modifier: Modifier = Modifier,
    onPairingStateChanged: ((PairingState) -> Unit)? = null,
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
                                    let state = 'UNKNOWN';
                                    
                                    // A. Real QR code present
                                    if (document.querySelector('canvas, img[src*="qr"]')) {
                                        state = 'QR_REQUIRED';
                                    }
                                    // B. URL-QR fallback
                                    else if (document.body.innerText.includes('messages.google.com/web/authentication')) {
                                        state = 'IMAGE_MATCH_REQUIRED';
                                    }
                                    // C. Image-match challenge
                                    else if (document.querySelector('[role="button"][data-mdc-dialog-action]') ||
                                             document.querySelector('img[src*="challenge"], div[data-challenge]')) {
                                        state = 'IMAGE_MATCH_REQUIRED';
                                    }
                                    // D. Full Messages UI loaded
                                    else if (document.querySelector('mws-conversation-list, div[aria-label="Conversations"]')) {
                                        state = 'PAIRED';
                                    }
                                    
                                    AndroidPairing.onPairingStateChanged(state);
                                }
                                
                                checkPairingState();
                                setInterval(checkPairingState, 1000);
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
