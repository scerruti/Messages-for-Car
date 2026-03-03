package com.otabi.messagesforcar.ui.qr

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.util.Log
import android.webkit.*
import androidx.compose.ui.viewinterop.AndroidView

// User agent for Google Messages Web
private const val WEBVIEW_USER_AGENT_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

/**
 * Account-based pairing screen using Google Messages for Web sign-in.
 * Loads Messages for Web, auto-initiates sign-in, and waits for account verification.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingAuthScreen(
    modifier: Modifier = Modifier,
    onPairingComplete: () -> Unit = {},
    onRetry: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var hasInjectedSignInCoordinator by remember { mutableStateOf(false) }
    var lastProcessedUrl by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header
            Text(
                text = "Pair Your Phone",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp // Automotive-optimized size
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Sign in to your Google account to pair this device with your phone.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp // Automotive-optimized size
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            // WebView for Messages for Web sign-in
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportMultipleWindows(false)
                                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = WEBVIEW_USER_AGENT_DESKTOP
                                }
                            
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                    consoleMessage?.let {
                                        val message = it.message()
                                        val isKnownNoise =
                                            message.contains("preloaded using link preload", ignoreCase = true) ||
                                            message.contains("Event handler of", ignoreCase = true)

                                        val isDebugSignal =
                                            message.contains("MessagesForCar:", ignoreCase = false) ||
                                            it.messageLevel() == ConsoleMessage.MessageLevel.ERROR

                                        if (!isKnownNoise && isDebugSignal) {
                                            Log.d(
                                                "WebViewConsole",
                                                "${it.messageLevel()}: $message (line ${it.lineNumber()} in ${it.sourceId()})"
                                            )
                                        }
                                    }
                                    return super.onConsoleMessage(consoleMessage)
                                }
                                
                                override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                    Log.d("WebViewAlert", "Alert from $url: $message")
                                    result?.confirm()
                                    return true
                                }
                                
                                override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                                    Log.d("WebViewConfirm", "Confirm from $url: $message")
                                    result?.confirm()
                                    return true
                                }
                                
                                override fun onJsPrompt(view: WebView?, url: String?, message: String?, defaultValue: String?, result: JsPromptResult?): Boolean {
                                    Log.d("WebViewPrompt", "Prompt from $url: $message")
                                    result?.confirm()
                                    return true
                                }
                            }
                            
                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                    Log.e("WebViewError", "Error loading ${request?.url}: ${error?.description}")
                                    super.onReceivedError(view, request, error)
                                }
                                
                                override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                                    Log.e("WebViewHttpError", "HTTP error for ${request?.url}: ${errorResponse?.statusCode} ${errorResponse?.reasonPhrase}")
                                    super.onReceivedHttpError(view, request, errorResponse)
                                }
                                
                                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                    Log.d("WebViewRequest", "Intercepting request: ${request?.url}")
                                    return super.shouldInterceptRequest(view, request)
                                }
                                
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    Log.d("WebViewPageFinished", "Page finished loading: $url")

                                    val currentUrl = url ?: return

                                    if (currentUrl == lastProcessedUrl) {
                                        Log.d("WebViewPageFinished", "Skipping duplicate onPageFinished for URL: $currentUrl")
                                        super.onPageFinished(view, url)
                                        return
                                    }

                                    lastProcessedUrl = currentUrl

                                    if (!hasInjectedSignInCoordinator) {
                                        hasInjectedSignInCoordinator = true

                                        val coordinatorScript = """
                                            (function() {
                                                if (window.__messagesForCarSignInCoordinatorStarted) {
                                                    console.log('MessagesForCar: Sign-in coordinator already started');
                                                    return 'already-started';
                                                }

                                                window.__messagesForCarSignInCoordinatorStarted = true;
                                                window.__messagesForCarSignInClicked = false;
                                                window.__messagesForCarMutationCount = 0;

                                                function tryClickSignIn() {
                                                    var candidates = document.querySelectorAll('button, div[role="button"], a');
                                                    console.log('MessagesForCar: Found ' + candidates.length + ' button/link candidates');
                                                    for (var i = 0; i < candidates.length; i++) {
                                                        var text = ((candidates[i].textContent || candidates[i].innerText || '') + '').trim().toLowerCase();
                                                        var aria = ((candidates[i].getAttribute('aria-label') || '') + '').trim().toLowerCase();
                                                        var fullText = text + ' | ' + aria;
                                                        if (fullText.length > 2) {
                                                            console.log('MessagesForCar: Button #' + i + ': "' + fullText.substring(0, 100) + '"');
                                                        }
                                                        if (text.includes('sign in') || aria.includes('sign in')) {
                                                            console.log('MessagesForCar: Found Sign in button. Clicking...');
                                                            candidates[i].click();
                                                            return true;
                                                        }
                                                    }
                                                    console.log('MessagesForCar: Sign in button not found');
                                                    return false;
                                                }

                                                if (!window.__messagesForCarSignInClicked) {
                                                    if (tryClickSignIn()) {
                                                        window.__messagesForCarSignInClicked = true;
                                                        console.log('MessagesForCar: Sign-in initiated');
                                                    }
                                                }

                                                var observer = new MutationObserver(function() {
                                                    window.__messagesForCarMutationCount++;
                                                    if (window.__messagesForCarMutationCount % 50 === 0) {
                                                        console.log('MessagesForCar: DOM mutations observed: ' + window.__messagesForCarMutationCount);
                                                    }
                                                    // Try to find sign-in button on every mutation if not clicked yet
                                                    if (!window.__messagesForCarSignInClicked) {
                                                        if (tryClickSignIn()) {
                                                            window.__messagesForCarSignInClicked = true;
                                                            console.log('MessagesForCar: Sign-in initiated (from mutation)');
                                                        }
                                                    }
                                                });
                                                observer.observe(document.documentElement || document.body, {
                                                    childList: true,
                                                    subtree: true,
                                                    attributes: true
                                                });
                                                window.__messagesForCarObserver = observer;

                                                console.log('MessagesForCar: Sign-in coordinator started');
                                                return 'coordinator-started';
                                            })();
                                        """.trimIndent()

                                        view?.evaluateJavascript(coordinatorScript) { result ->
                                            Log.d("WebViewCoordinator", "Coordinator result: $result")
                                        }
                                    } else {
                                        Log.d("WebViewCoordinator", "Skipping coordinator injection; already injected")
                                    }
                                    
                                    super.onPageFinished(view, url)
                                }
                            }
                            
                            // Load Messages for Web
                            loadUrl("https://messages.google.com/web")
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Instructions:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val instructions = listOf(
                        "1. Wait for Messages for Web to load in the WebView above",
                        "2. The app will attempt to click Sign in automatically",
                        "3. Complete Google account sign-in in the embedded page",
                        "4. Once signed in, click the button below to complete pairing",
                        "5. Return to your phone to verify pairing"
                    )
                    
                    instructions.forEach { instruction ->
                        Text(
                            text = instruction,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 16.sp
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Button
            Button(
                onClick = onPairingComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Automotive-compliant touch target
                enabled = !isLoading
            ) {
                Text(
                    text = "Pairing Complete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
