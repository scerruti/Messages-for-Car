package com.otabi.messagesforcar.ui.qr

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color as AndroidColor
import android.util.Log
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: Add spinner/UX state when observer waits longer than this timeout.
private const val WEBVIEW_QR_OBSERVER_TIMEOUT_MS = 15000L
private const val WEBVIEW_MIN_QR_DATA_URL_LENGTH = 1000
private const val WEBVIEW_USER_AGENT_MOBILE = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
private const val WEBVIEW_USER_AGENT_DESKTOP = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

private enum class PairingAuthMode {
    QR,
    SIGN_IN
}

private fun applyAuthModeWebSettings(webView: WebView, mode: PairingAuthMode) {
    webView.settings.userAgentString = if (mode == PairingAuthMode.SIGN_IN) {
        WEBVIEW_USER_AGENT_DESKTOP
    } else {
        WEBVIEW_USER_AGENT_MOBILE
    }
}

/**
 * Native QR Code Pairing Screen using ZXing library.
 * Replaces unreliable WebView QR code implementation with automotive-optimized native UI.
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
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrCodeDataUrl by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) } // Disabled for WebView debugging
    var generationError by remember { mutableStateOf<String?>(null) }
    var authMode by remember { mutableStateOf(PairingAuthMode.SIGN_IN) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var hasCapturedQr by remember { mutableStateOf(false) }
    var hasAttemptedPairClick by remember { mutableStateOf(false) }
    var hasInjectedQrCoordinator by remember { mutableStateOf(false) }
    var lastProcessedUrl by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // QR code generation disabled - using WebView for debugging Messages for Web
    /*
    LaunchedEffect(Unit) {
        try {
            isGenerating = true
            generationError = null
            
            // Google Messages Web authentication URL for QR pairing
            val pairingUrl = "https://messages.google.com/web/authentication"
            val qrCode = generateQRCode(pairingUrl, 512, 512)
            qrCodeBitmap = qrCode
            isGenerating = false
            
            Log.d("QRCodePairing", "QR code generated successfully for URL: $pairingUrl")
        } catch (e: Exception) {
            Log.e("QRCodePairing", "Failed to generate QR code", e)
            generationError = "Failed to generate QR code: ${e.message}"
            isGenerating = false
        }
    }
    */
    
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
                text = if (authMode == PairingAuthMode.QR)
                    "Use Google Messages Web to generate QR code"
                else
                    "Use Google account sign-in to pair",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp // Automotive-optimized size
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = authMode == PairingAuthMode.QR,
                    onClick = {
                        authMode = PairingAuthMode.QR
                        hasCapturedQr = false
                        hasAttemptedPairClick = false
                        hasInjectedQrCoordinator = false
                        lastProcessedUrl = null
                        qrCodeBitmap = null
                        generationError = null
                        webViewRef?.let { applyAuthModeWebSettings(it, authMode) }
                        webViewRef?.loadUrl("https://messages.google.com/web")
                    },
                    label = { Text("QR Pairing") }
                )

                FilterChip(
                    selected = authMode == PairingAuthMode.SIGN_IN,
                    onClick = {
                        authMode = PairingAuthMode.SIGN_IN
                        hasCapturedQr = false
                        hasAttemptedPairClick = false
                        hasInjectedQrCoordinator = false
                        lastProcessedUrl = null
                        qrCodeBitmap = null
                        generationError = null
                        webViewRef?.let { applyAuthModeWebSettings(it, authMode) }
                        webViewRef?.loadUrl("https://messages.google.com/web")
                    },
                    label = { Text("Account Sign-In") }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            val webViewCardModifier = if (authMode == PairingAuthMode.SIGN_IN) {
                Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            } else {
                Modifier.size(320.dp)
            }
            
            // WebView Display Area for debugging Messages for Web QR generation
            Card(
                modifier = webViewCardModifier,
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
                            webViewRef = this
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
                            }
                            applyAuthModeWebSettings(this, authMode)
                            
                            // JavaScript interface to receive QR code data from web page
                            addJavascriptInterface(object {
                                @android.webkit.JavascriptInterface
                                fun onQRCodeFound(dataUrl: String) {
                                    Log.d("WebViewQRCapture", "QR code data received, length: ${dataUrl.length}")
                                    qrCodeDataUrl = dataUrl
                                    hasCapturedQr = true
                                    hasInjectedQrCoordinator = true
                                    // Decode the data URL to a bitmap for display
                                    try {
                                        val base64String = dataUrl.substringAfter("base64,")
                                        val decodedBytes = android.util.Base64.decode(base64String, android.util.Base64.DEFAULT)
                                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                        qrCodeBitmap = bitmap
                                        isGenerating = false
                                        Log.d("WebViewQRCapture", "QR code bitmap decoded successfully")
                                    } catch (e: Exception) {
                                        Log.e("WebViewQRCapture", "Failed to decode QR data URL", e)
                                        generationError = "Failed to decode QR code"
                                    }
                                }
                            }, "messagesForCar")
                            
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

                                    if (hasCapturedQr) {
                                        Log.d("WebViewPageFinished", "QR already captured; skipping further auto-click/extraction")
                                        super.onPageFinished(view, url)
                                        return
                                    }

                                    if (!hasInjectedQrCoordinator) {
                                        hasInjectedQrCoordinator = true
                                        if (currentUrl.contains("/web/welcome")) {
                                            hasAttemptedPairClick = true
                                        }

                                        val preferSignIn = authMode == PairingAuthMode.SIGN_IN

                                        val coordinatorScript = """
                                            (function() {
                                                if (window.__messagesForCarCoordinatorStarted) {
                                                    console.log('MessagesForCar: Coordinator already started');
                                                    return 'already-started';
                                                }

                                                window.__messagesForCarCoordinatorStarted = true;
                                                window.__messagesForCarSignInClicked = window.__messagesForCarSignInClicked || false;
                                                window.__messagesForCarPairActionClicked = window.__messagesForCarPairActionClicked || false;
                                                window.__messagesForCarLastQrDataUrl = window.__messagesForCarLastQrDataUrl || null;
                                                window.__messagesForCarQrCaptures = window.__messagesForCarQrCaptures || 0;
                                                window.__messagesForCarMutationCount = window.__messagesForCarMutationCount || 0;

                                                function sendQrData(dataUrl, source) {
                                                    if (!dataUrl || dataUrl.length < $WEBVIEW_MIN_QR_DATA_URL_LENGTH) {
                                                        return false;
                                                    }
                                                    
                                                    // Only send if this is a new/different QR code
                                                    if (window.__messagesForCarLastQrDataUrl === dataUrl) {
                                                        return false;
                                                    }
                                                    
                                                    window.__messagesForCarLastQrDataUrl = dataUrl;
                                                    window.__messagesForCarQrCaptures = (window.__messagesForCarQrCaptures || 0) + 1;
                                                    console.log('MessagesForCar: Found ' + source + ' QR code #' + window.__messagesForCarQrCaptures + ', data URL length: ' + dataUrl.length);
                                                    window.messagesForCar.onQRCodeFound(dataUrl);
                                                    return true;
                                                }

                                                function tryCaptureQr() {
                                                    var canvas = document.querySelector('canvas');
                                                    if (canvas) {
                                                        console.log('MessagesForCar: Found canvas element');
                                                        try {
                                                            var dataUrl = canvas.toDataURL('image/png');
                                                            console.log('MessagesForCar: Canvas data URL length: ' + dataUrl.length);
                                                            if (sendQrData(dataUrl, 'canvas')) {
                                                                return true;
                                                            }
                                                        } catch (e) {
                                                            console.log('MessagesForCar: Error extracting canvas: ' + e.message);
                                                        }
                                                    } else {
                                                        console.log('MessagesForCar: No canvas element found');
                                                    }

                                                    var imgs = document.querySelectorAll('img[src^="data:image"]');
                                                    console.log('MessagesForCar: Found ' + imgs.length + ' data:image elements');
                                                    for (var i = 0; i < imgs.length; i++) {
                                                        console.log('MessagesForCar: Checking data:image #' + (i+1) + ', src length: ' + imgs[i].src.length);
                                                        if (sendQrData(imgs[i].src, 'data:image')) {
                                                            return true;
                                                        }
                                                    }

                                                    return false;
                                                }

                                                function tryClickPairButton() {
                                                    var candidates = document.querySelectorAll('button, div[role="button"], a');
                                                    console.log('MessagesForCar: Found ' + candidates.length + ' button/link candidates');
                                                    var desiredMode = ${if (preferSignIn) "'sign-in'" else "'qr'"};

                                                    function clickCandidate(textMatchFn, successLog) {
                                                        for (var i = 0; i < candidates.length; i++) {
                                                            var text = ((candidates[i].textContent || candidates[i].innerText || '') + '').trim().toLowerCase();
                                                            var aria = ((candidates[i].getAttribute('aria-label') || '') + '').trim().toLowerCase();
                                                            var fullText = text + ' | ' + aria;
                                                            if (fullText.length > 2) {
                                                                console.log('MessagesForCar: Button #' + i + ': "' + fullText.substring(0, 80) + '"');
                                                            }
                                                            if (textMatchFn(text, aria)) {
                                                                console.log(successLog);
                                                                candidates[i].click();
                                                                return true;
                                                            }
                                                        }
                                                        return false;
                                                    }

                                                    if (desiredMode === 'sign-in') {
                                                        if (!window.__messagesForCarSignInClicked) {
                                                            if (clickCandidate(
                                                                function(text, aria) { return text.includes('sign in') || aria.includes('sign in'); },
                                                                'MessagesForCar: Found Sign in button. Clicking...'
                                                            )) {
                                                                window.__messagesForCarSignInClicked = true;
                                                                return true;
                                                            }
                                                        }

                                                        if (!window.__messagesForCarPairActionClicked) {
                                                            console.log('MessagesForCar: Looking for pairing action button (pair/link/device/connect)');
                                                            if (clickCandidate(
                                                                function(text, aria) {
                                                                    return text.includes('pair') || aria.includes('pair') || text.includes('link') || aria.includes('link') || text.includes('device') || aria.includes('device') || text.includes('connect') || aria.includes('connect');
                                                                },
                                                                'MessagesForCar: Found post-login pairing action. Clicking...'
                                                            )) {
                                                                window.__messagesForCarPairActionClicked = true;
                                                                return true;
                                                            }
                                                        }

                                                        return false;
                                                    }

                                                    if (!window.__messagesForCarPairActionClicked) {
                                                        if (clickCandidate(
                                                            function(text, aria) {
                                                                return (text.includes('pair') && text.includes('qr')) || (aria.includes('pair') && aria.includes('qr'));
                                                            },
                                                            'MessagesForCar: Found Pair with QR button. Clicking...'
                                                        )) {
                                                            window.__messagesForCarPairActionClicked = true;
                                                            return true;
                                                        }
                                                    }

                                                    if (desiredMode !== 'sign-in') {
                                                        if (!window.__messagesForCarSignInClicked) {
                                                            if (clickCandidate(
                                                                function(text, aria) { return text.includes('sign in') || aria.includes('sign in'); },
                                                                'MessagesForCar: QR option unavailable, falling back to Sign in button. Clicking...'
                                                            )) {
                                                                window.__messagesForCarSignInClicked = true;
                                                                return true;
                                                            }
                                                        }
                                                    }
                                                    return false;
                                                }

                                                function processDom(reason) {
                                                    var clicked = tryClickPairButton();
                                                    var captured = tryCaptureQr();
                                                    if (!captured && (clicked || reason === 'initial')) {
                                                        console.log('MessagesForCar: Waiting for QR/icon after ' + reason + ' (signInClicked=' + window.__messagesForCarSignInClicked + ', pairClicked=' + window.__messagesForCarPairActionClicked + ')');
                                                    }
                                                }

                                                console.log('MessagesForCar: Coordinator starting, authMode=' + ${if (preferSignIn) "'sign-in'" else "'qr'"});
                                                processDom('initial');

                                                var observer = new MutationObserver(function() {
                                                    window.__messagesForCarMutationCount++;
                                                    if (window.__messagesForCarMutationCount % 10 === 0) {
                                                        console.log('MessagesForCar: Mutation #' + window.__messagesForCarMutationCount);
                                                    }
                                                    processDom('mutation');
                                                });
                                                observer.observe(document.documentElement || document.body, {
                                                    childList: true,
                                                    subtree: true,
                                                    attributes: true
                                                });
                                                window.__messagesForCarObserver = observer;

                                                setTimeout(function() {
                                                    console.log('MessagesForCar: Coordinator timeout after $WEBVIEW_QR_OBSERVER_TIMEOUT_MS ms - mutations: ' + window.__messagesForCarMutationCount + ', QR captures: ' + window.__messagesForCarQrCaptures + ', signInClicked: ' + window.__messagesForCarSignInClicked + ', pairClicked: ' + window.__messagesForCarPairActionClicked);
                                                }, $WEBVIEW_QR_OBSERVER_TIMEOUT_MS);

                                                return 'coordinator-started';
                                            })();
                                        """.trimIndent()

                                        view?.evaluateJavascript(coordinatorScript) { }
                                    } else {
                                        Log.d("WebViewQR", "Skipping coordinator injection; already injected or captured")
                                    }
                                    
                                    super.onPageFinished(view, url)
                                }
                            }
                            
                            // Load Messages for Web QR pairing
                            // Must authenticate first, then navigate to pairing flow
                            loadUrl("https://messages.google.com/web")
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )

                    if (qrCodeBitmap != null && authMode == PairingAuthMode.QR) {
                        Image(
                            bitmap = qrCodeBitmap!!.asImageBitmap(),
                            contentDescription = "Captured QR code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .background(Color.White)
                        )
                    }
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
                    
                    val instructions = if (authMode == PairingAuthMode.QR) {
                        listOf(
                            "1. Wait for Messages for Web to load in the WebView above",
                            "2. The app will attempt to open Pair with QR automatically",
                            "3. If prompted, allow required permissions",
                            "4. The captured QR will be shown in this card",
                            "5. Scan it with Google Messages on your phone"
                        )
                    } else {
                        listOf(
                            "1. Wait for Messages for Web to load in the WebView above",
                            "2. The app will attempt to click Sign in automatically",
                            "3. Complete Google account sign-in in the embedded page",
                            "4. Continue pairing using account-based flow",
                            "5. Use Back to return when pairing is complete"
                        )
                    }
                    
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
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Retry button
                Button(
                    onClick = {
                        generationError = null
                        onRetry()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp), // Automotive-compliant touch target
                    enabled = !isLoading && !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    } else {
                        Text(
                            text = "Refresh QR Code",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Skip/Continue button for testing
                OutlinedButton(
                    onClick = onPairingComplete,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp), // Automotive-compliant touch target
                    enabled = !isLoading && !isGenerating
                ) {
                    Text(
                        text = "I Paired Successfully",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Generate QR code bitmap using ZXing library.
 * Optimized for automotive displays with high contrast and clear visibility.
 */
private suspend fun generateQRCode(
    text: String,
    width: Int,
    height: Int
): Bitmap = withContext(Dispatchers.Default) {
    try {
        val writer = MultiFormatWriter()
        val hints = mapOf(
            EncodeHintType.MARGIN to 2, // Minimal margin for larger code
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        
        val bitMatrix: BitMatrix = writer.encode(
            text,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        
        // Fill background with white
        canvas.drawColor(AndroidColor.WHITE)
        
        // Draw QR code pixels
        paint.color = AndroidColor.BLACK
        for (x in 0 until width) {
            for (y in 0 until height) {
                if (bitMatrix[x, y]) {
                    canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
                }
            }
        }
        
        bitmap
    } catch (e: Exception) {
        Log.e("QRCodeGeneration", "Failed to generate QR code", e)
        throw e
    }
}
