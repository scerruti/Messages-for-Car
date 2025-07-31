package com.scerruti.messagesforcar.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.JavascriptInterface
import android.util.Log

/**
 * JavaScript interface for detecting pairing state changes
 */
class PairingStateInterface(private val onPairingStateChanged: ((Boolean) -> Unit)?) {
    @JavascriptInterface
    fun onPairingDetected(isPaired: Boolean) {
        Log.d("PairingState", "Pairing state changed: $isPaired")
        onPairingStateChanged?.invoke(isPaired)
    }
}

@Composable
fun MessagingWebView(
    modifier: Modifier = Modifier,
    onPairingStateChanged: ((Boolean) -> Unit)? = null
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                // Add JavaScript interface for pairing detection
                addJavascriptInterface(PairingStateInterface(onPairingStateChanged), "AndroidPairing")
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        Log.d("WebView", "shouldOverrideUrlLoading: $url")
                        // Allow navigation within Google Messages domain
                        return if (url?.contains("messages.google.com") == true || url?.contains("accounts.google.com") == true) {
                            false
                        } else {
                            // Block external navigation for security
                            Log.d("WebView", "Blocking external URL: $url")
                            true
                        }
                    }
                    
                    override fun onReceivedError(view: WebView?, errorCode: Int, description: String?, failingUrl: String?) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e("WebView", "Error loading $failingUrl: $errorCode - $description")
                        
                        // Load a simple error page with retry button
                        view?.loadData("""
                            <html>
                            <head><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
                            <body style="font-family: Arial; padding: 20px; text-align: center; background-color: #f5f5f5;">
                                <h2 style="color: #1976D2;">Connection Error</h2>
                                <p>Unable to load Google Messages for Web</p>
                                <p><strong>Error:</strong> $description</p>
                                <p><strong>URL:</strong> $failingUrl</p>
                                <button onclick="window.location.href='https://messages.google.com/web'" 
                                        style="background-color: #1976D2; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-size: 16px; cursor: pointer;">
                                    Try Again
                                </button>
                                <br><br>
                                <button onclick="window.location.href='https://www.google.com'" 
                                        style="background-color: #4CAF50; color: white; padding: 12px 24px; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; margin-top: 10px;">
                                    Test Google.com
                                </button>
                            </body>
                            </html>
                        """.trimIndent(), "text/html", "UTF-8")
                    }
                    
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("WebView", "Page started loading: $url")
                    }
                    
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("WebView", "Page finished loading: $url")
                        
                        // If we're on welcome page, navigate to authentication for QR code
                        if (url?.contains("/web/welcome") == true) {
                            Log.d("WebView", "On welcome page, navigating to authentication...")
                            view?.loadUrl("https://messages.google.com/web/authentication")
                            return
                        }
                        
                        // Inject pairing detection and automotive optimizations
                        view?.evaluateJavascript("""
                            (function() {
                                console.log('Injecting pairing detection and automotive styles');
                                
                                // Function to check pairing state
                                function checkPairingState() {
                                    console.log('=== PAGE DEBUG INFO ===');
                                    console.log('Document title:', document.title);
                                    console.log('Document URL:', window.location.href);
                                    console.log('Document body innerHTML length:', document.body ? document.body.innerHTML.length : 'NO BODY');
                                    console.log('Document body classes:', document.body ? document.body.className : 'NO BODY');
                                    
                                    // Check for any QR code related elements
                                    const qrSelectors = [
                                        '[data-e2e-name="qr-code"]',
                                        '.qr-code',
                                        '[aria-label*="QR"]',
                                        '[title*="QR"]',
                                        '[data-testid="qr-code"]',
                                        '.qr-code-container',
                                        'canvas',
                                        'svg',
                                        '[alt*="QR"]',
                                        '[alt*="qr"]'
                                    ];
                                    
                                    let qrFound = false;
                                    qrSelectors.forEach(selector => {
                                        const elements = document.querySelectorAll(selector);
                                        if (elements.length > 0) {
                                            console.log('Found QR elements with selector', selector, ':', elements.length);
                                            qrFound = true;
                                        }
                                    });
                                    
                                    // Check for conversation elements
                                    const conversationSelectors = [
                                        '[data-e2e-name="conversation-list"]',
                                        '.conversation-list',
                                        '[aria-label*="conversation"]',
                                        '.conversations',
                                        '[data-e2e-name="message-area"]',
                                        '.message-area',
                                        '[contenteditable]',
                                        'textarea'
                                    ];
                                    
                                    let conversationFound = false;
                                    conversationSelectors.forEach(selector => {
                                        const elements = document.querySelectorAll(selector);
                                        if (elements.length > 0) {
                                            console.log('Found conversation elements with selector', selector, ':', elements.length);
                                            conversationFound = true;
                                        }
                                    });
                                    
                                    // Check for any text that might indicate state
                                    const bodyText = document.body ? document.body.innerText : '';
                                    const hasQRText = bodyText.toLowerCase().includes('qr') || bodyText.toLowerCase().includes('scan');
                                    const hasPairText = bodyText.toLowerCase().includes('pair');
                                    const hasConnectText = bodyText.toLowerCase().includes('connect');
                                    
                                    console.log('Body text contains QR/scan:', hasQRText);
                                    console.log('Body text contains pair:', hasPairText);
                                    console.log('Body text contains connect:', hasConnectText);
                                    console.log('Body text sample (first 200 chars):', bodyText.substring(0, 200));
                                    
                                    const isPaired = conversationFound && !qrFound;
                                    const isUnpaired = qrFound || hasQRText || hasPairText;
                                    
                                    console.log('Final state - QR found:', qrFound, 'Conversation found:', conversationFound, 'Determined paired:', isPaired);
                                    
                                    if (window.lastPairingState !== isPaired) {
                                        window.lastPairingState = isPaired;
                                        console.log('Pairing state changed to:', isPaired ? 'PAIRED' : 'UNPAIRED');
                                        AndroidPairing.onPairingDetected(isPaired);
                                    }
                                }
                                
                                // Check pairing state immediately and periodically
                                checkPairingState();
                                setInterval(checkPairingState, 2000);
                                
                                // Observer for DOM changes
                                const observer = new MutationObserver(function(mutations) {
                                    mutations.forEach(function(mutation) {
                                        if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                                            setTimeout(checkPairingState, 500);
                                        }
                                    });
                                });
                                
                                observer.observe(document.body, { 
                                    childList: true, 
                                    subtree: true 
                                });
                                
                                // Add automotive-specific CSS adjustments
                                const style = document.createElement('style');
                                style.textContent = `
                                    body { 
                                        font-size: 1.2em !important; 
                                        background: white !important;
                                        overflow: visible !important;
                                    }
                                    .conversation-list { font-size: 1.1em !important; }
                                    button, input { min-height: 48px !important; }
                                    
                                    /* Ensure QR code is visible */
                                    .qr-code-container,
                                    canvas,
                                    svg[role="img"],
                                    [alt*="QR"],
                                    [alt*="qr"],
                                    [data-e2e-name="qr-code"],
                                    .qr-code {
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                        width: auto !important;
                                        height: auto !important;
                                        min-width: 200px !important;
                                        min-height: 200px !important;
                                        position: relative !important;
                                        z-index: 9999 !important;
                                        background: white !important;
                                        border: 2px solid #000 !important;
                                        margin: 20px !important;
                                        padding: 10px !important;
                                    }
                                    
                                    /* Force parent containers to be visible */
                                    .qr-code-container *,
                                    canvas *,
                                    svg[role="img"] * {
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                    }
                                    .qr-code { 
                                        transform: scale(1.2) !important; 
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                    }
                                    canvas { 
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                        background: white !important;
                                    }
                                    /* Force visibility of common QR code containers */
                                    [data-testid="qr-code"],
                                    [data-e2e-name="qr-code"],
                                    .qr-code-container,
                                    .qr-code,
                                    .pairing-container {
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                        background: white !important;
                                        min-height: 200px !important;
                                        min-width: 200px !important;
                                    }
                                    /* Fix any hidden or collapsed elements */
                                    * {
                                        max-height: none !important;
                                        max-width: none !important;
                                    }
                                    /* Make sure main content is visible */
                                    main, .main-content, .content, .app-content {
                                        display: block !important;
                                        visibility: visible !important;
                                        opacity: 1 !important;
                                    }
                                `;
                                document.head.appendChild(style);
                                
                                // Force refresh of any canvas elements and SVG elements immediately
                                setTimeout(() => {
                                    console.log('MessagesForCar: 2-second timeout - checking canvas/SVG elements');
                                    
                                    // Force canvas visibility with stronger styles
                                    const canvases = document.querySelectorAll('canvas');
                                    console.log('Found', canvases.length, 'canvas elements');
                                    canvases.forEach((canvas, index) => {
                                        console.log('Canvas', index, 'dimensions:', canvas.width + 'x' + canvas.height, 'visible:', canvas.offsetParent !== null);
                                        
                                        // Use cssText for strongest override but preserve QR code content
                                        canvas.style.cssText = `
                                            display: block !important;
                                            visibility: visible !important;
                                            opacity: 1 !important;
                                            position: relative !important;
                                            z-index: 99999 !important;
                                            width: 250px !important;
                                            height: 250px !important;
                                            background-color: white !important;
                                            border: 3px solid #1976d2 !important;
                                            margin: 20px auto !important;
                                            clear: both !important;
                                        `;
                                        
                                        // Also modify parent containers but preserve their layout
                                        let parent = canvas.parentElement;
                                        let depth = 0;
                                        while (parent && parent !== document.body && depth < 5) {
                                            parent.style.cssText += 'display: block !important; visibility: visible !important; opacity: 1 !important; overflow: visible !important; text-align: center !important;';
                                            parent = parent.parentElement;
                                            depth++;
                                        }
                                    });
                                    
                                    // Force SVG visibility with stronger styles
                                    const svgs = document.querySelectorAll('svg');
                                    console.log('Found', svgs.length, 'SVG elements');
                                    svgs.forEach((svg, index) => {
                                        const rect = svg.getBoundingClientRect();
                                        console.log('SVG', index, 'dimensions:', rect.width + 'x' + rect.height, 'visible:', svg.offsetParent !== null);
                                        
                                        // Force all SVGs to be visible with strong styles
                                        svg.style.cssText = `
                                            display: block !important;
                                            visibility: visible !important;
                                            opacity: 1 !important;
                                            position: relative !important;
                                            z-index: 99999 !important;
                                            width: 250px !important;
                                            height: 250px !important;
                                            background-color: white !important;
                                            border: 3px solid #4CAF50 !important;
                                            margin: 20px auto !important;
                                            clear: both !important;
                                        `;
                                        
                                        // Also modify parent containers
                                        let parent = svg.parentElement;
                                        let depth = 0;
                                        while (parent && parent !== document.body && depth < 5) {
                                            parent.style.cssText += 'display: block !important; visibility: visible !important; opacity: 1 !important; overflow: visible !important; text-align: center !important;';
                                            parent = parent.parentElement;
                                            depth++;
                                        }
                                    });
                                        while (parent && parent !== document.body) {
                                            parent.style.cssText += 'display: block !important; visibility: visible !important; opacity: 1 !important; overflow: visible !important;';
                                            parent = parent.parentElement;
                                        }
                                    });
                                    
                                    // Force QR code container visibility
                                    const qrContainers = document.querySelectorAll('.qr-code-container, [class*="qr"], [id*="qr"]');
                                    console.log('Found', qrContainers.length, 'QR containers');
                                    qrContainers.forEach((container, index) => {
                                        console.log('QR Container', index, 'forcing visibility');
                                        container.style.cssText = `
                                            display: block !important;
                                            visibility: visible !important;
                                            opacity: 1 !important;
                                            position: relative !important;
                                            z-index: 99999 !important;
                                            width: 350px !important;
                                            height: 350px !important;
                                            min-width: 350px !important;
                                            min-height: 350px !important;
                                            max-width: none !important;
                                            max-height: none !important;
                                            background-color: white !important;
                                            border: 5px solid green !important;
                                            margin: 20px auto !important;
                                            padding: 20px !important;
                                            clear: both !important;
                                            overflow: visible !important;
                                        `;
                                    });
                                }, 2000);
                                
                                // Try to force QR code regeneration after 4 seconds
                                setTimeout(() => {
                                    console.log('MessagesForCar: 4-second timeout - attempting QR regeneration');
                                    
                                    // Try to trigger QR code refresh by simulating events
                                    const qrContainers = document.querySelectorAll('.qr-code-container, [class*="qr"], [data-testid*="qr"]');
                                    qrContainers.forEach(container => {
                                        // Trigger mutation observer by temporarily hiding and showing
                                        const originalDisplay = container.style.display;
                                        container.style.display = 'none';
                                        setTimeout(() => {
                                            container.style.display = originalDisplay || 'block';
                                        }, 100);
                                    });
                                    
                                    // Try to refresh by triggering window resize
                                    window.dispatchEvent(new Event('resize'));
                                    
                                    // Force reload any canvas elements
                                    const canvases = document.querySelectorAll('canvas');
                                    canvases.forEach(canvas => {
                                        if (canvas.getContext) {
                                            const ctx = canvas.getContext('2d');
                                            if (ctx) {
                                                // Clear and redraw
                                                ctx.clearRect(0, 0, canvas.width, canvas.height);
                                                // Try to trigger a repaint
                                                canvas.style.transform = 'translateZ(0)';
                                            }
                                        }
                                    });
                                    
                                }, 4000);
                                
                                // Nuclear option after 5 seconds
                                setTimeout(() => {
                                    console.log('MessagesForCar: 5-second nuclear option - forcing all QR elements visible');
                                    
                                    // Make ANY element with QR-related attributes massive and visible
                                    document.querySelectorAll('[class*="qr"], [id*="qr"], [data-testid*="qr"], [aria-label*="QR"]').forEach((el, index) => {
                                        console.log('Forcing QR element', index, 'to be visible');
                                        el.style.cssText = `
                                            display: block !important;
                                            visibility: visible !important;
                                            opacity: 1 !important;
                                            width: 300px !important;
                                            height: 300px !important;
                                            border: 5px solid green !important;
                                            background: white !important;
                                            position: relative !important;
                                            z-index: 999999 !important;
                                            margin: 20px auto !important;
                                        `;
                                    });
                                    
                                    // If still no visible QR code, inject fallback message using safer method
                                    if (document.querySelectorAll('canvas[style*="red"], svg[style*="blue"]').length === 0) {
                                        console.log('No QR code visible after 5 seconds - injecting fallback');
                                        const fallbackDiv = document.createElement('div');
                                        fallbackDiv.style.cssText = `
                                            position: fixed !important;
                                            top: 200px !important;
                                            left: 50% !important;
                                            transform: translateX(-50%) !important;
                                            width: 400px !important;
                                            height: 200px !important;
                                            background: #e3f2fd !important;
                                            border: 3px solid #1976d2 !important;
                                            border-radius: 8px !important;
                                            padding: 20px !important;
                                            text-align: center !important;
                                            z-index: 999999 !important;
                                            font-family: Arial !important;
                                            color: #1976d2 !important;
                                        `;
                                        
                                        // Create elements safely
                                        const title = document.createElement('h3');
                                        title.textContent = 'QR Code Not Visible';
                                        title.style.cssText = 'color: #1976d2 !important; margin: 0 0 10px 0 !important;';
                                        
                                        const p1 = document.createElement('p');
                                        p1.textContent = 'Elements detected but not rendering. Try:';
                                        p1.style.cssText = 'margin: 5px 0 !important;';
                                        
                                        const p2 = document.createElement('p');
                                        p2.textContent = '1. Refresh the page';
                                        p2.style.cssText = 'margin: 5px 0 !important;';
                                        
                                        const p3 = document.createElement('p');
                                        p3.textContent = '2. Clear browser cache';
                                        p3.style.cssText = 'margin: 5px 0 !important;';
                                        
                                        const p4 = document.createElement('p');
                                        p4.textContent = '3. Use phone to visit messages.google.com/web';
                                        p4.style.cssText = 'margin: 5px 0 !important;';
                                        
                                        fallbackDiv.appendChild(title);
                                        fallbackDiv.appendChild(p1);
                                        fallbackDiv.appendChild(p2);
                                        fallbackDiv.appendChild(p3);
                                        fallbackDiv.appendChild(p4);
                                        
                                        document.body.appendChild(fallbackDiv);
                                    }
                                }, 5000);
                            })();
                        """.trimIndent(), null)
                    }
                }
                
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("WebView", "Console: ${consoleMessage?.message()}")
                        return true
                    }
                }
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    @Suppress("DEPRECATION")
                    databaseEnabled = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    cacheMode = WebSettings.LOAD_DEFAULT
                    
                    // Use desktop user agent for full interface compatibility
                    userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    
                    // Additional WebView settings for optimal rendering
                    setGeolocationEnabled(false)
                    javaScriptCanOpenWindowsAutomatically = false
                    setSupportMultipleWindows(false)
                    allowFileAccess = true
                    allowContentAccess = true
                    setRenderPriority(WebSettings.RenderPriority.HIGH)
                    mediaPlaybackRequiresUserGesture = false
                }
                
                // Enable WebView debugging in debug builds
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    android.webkit.WebView.setWebContentsDebuggingEnabled(true)
                }
                
                // Load Google Messages for Web
                Log.d("WebView", "Loading URL: https://messages.google.com/web")
                loadUrl("https://messages.google.com/web/authentication")
            }
        }
    )
}
