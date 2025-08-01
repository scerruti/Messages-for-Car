package com.scerruti.messagesforcar.ui.qr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.scerruti.messagesforcar.automotive.AutomotiveHelper
import com.scerruti.messagesforcar.ui.theme.MessagesForCarTheme
import com.scerruti.messagesforcar.data.preferences.PairingPreferences
import kotlinx.coroutines.launch

/**
 * Automotive-optimized QR Code Pairing Activity.
 * Provides a full-screen native QR code pairing experience that replaces
 * the unreliable WebView-based implementation.
 */
class QRCodePairingActivity : ComponentActivity() {
    
    private lateinit var pairingStateManager: PairingStateManager
    private lateinit var pairingPreferences: PairingPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        pairingStateManager = PairingStateManager(this)
        pairingPreferences = PairingPreferences.getInstance(this)
        
        setContent {
            MessagesForCarTheme {
                QRCodePairingActivityContent(
                    pairingStateManager = pairingStateManager,
                    onPairingComplete = { finishPairingFlow() },
                    onCancel = { finish() }
                )
            }
        }
        
        // Check initial pairing state
        lifecycleScope.launch {
            pairingStateManager.checkPairingStatus()
        }
    }
    
    private fun finishPairingFlow() {
        // Mark as paired in preferences
        val pairingUrl = "https://messages.google.com/web" // Default URL for now
        pairingPreferences.markAsPaired(pairingUrl)
        
        pairingStateManager.markPairingSuccessful()
        
        // Return result to calling activity
        setResult(RESULT_OK)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QRCodePairingActivityContent(
    pairingStateManager: PairingStateManager,
    onPairingComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val pairingState by pairingStateManager.collectPairingStateAsState()
    val isCheckingPairing by pairingStateManager.collectIsCheckingPairingAsState()
    val scrollState = rememberScrollState()
    
    var showQRCode by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Handle pairing state changes
    LaunchedEffect(pairingState) {
        when (pairingState) {
            PairingState.PAIRED -> {
                showQRCode = false
                // Auto-complete after a short delay to show success state
                kotlinx.coroutines.delay(2000)
                onPairingComplete()
            }
            PairingState.ERROR -> {
                errorMessage = "Failed to check pairing status. Please try again."
            }
            else -> {
                showQRCode = pairingStateManager.shouldShowQRCode()
                errorMessage = null
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pair with Google Messages",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    pairingState == PairingState.PAIRED -> {
                        // Success state
                        PairingSuccessScreen(
                            onContinue = onPairingComplete
                        )
                    }
                    
                    showQRCode -> {
                        // QR Code pairing screen
                        QRCodePairingScreen(
                            modifier = Modifier.fillMaxWidth(),
                            onPairingComplete = onPairingComplete,
                            onRetry = {
                                errorMessage = null
                                kotlinx.coroutines.MainScope().launch {
                                    pairingStateManager.checkPairingStatus()
                                }
                            },
                            isLoading = isCheckingPairing,
                            errorMessage = errorMessage
                        )
                    }
                    
                    else -> {
                        // Loading or checking state
                        PairingLoadingScreen(
                            pairingState = pairingState,
                            onRetry = {
                                kotlinx.coroutines.MainScope().launch {
                                    pairingStateManager.checkPairingStatus()
                                }
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Automotive-specific information
                if (AutomotiveHelper.isAutomotiveOS(LocalContext.current)) {
                    AutomotivePairingInfo()
                }
            }
            
            // Floating action for manual pairing check
            FloatingActionButton(
                onClick = {
                    kotlinx.coroutines.MainScope().launch {
                        pairingStateManager.checkPairingStatus()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                if (isCheckingPairing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                        contentDescription = "Check pairing status",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun PairingSuccessScreen(
    onContinue: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pairing Successful!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your car is now connected to Google Messages. You can receive and reply to messages safely while driving.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue to Messages")
                }
            }
        }
    }
}

@Composable
private fun PairingLoadingScreen(
    pairingState: PairingState,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when (pairingState) {
                PairingState.UNKNOWN -> "Checking pairing status..."
                PairingState.EXPIRED -> "Reconnecting to Google Messages..."
                else -> "Loading..."
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        if (pairingState == PairingState.ERROR) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun AutomotivePairingInfo() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚗",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Automotive Safety",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "• QR code pairing is only available when parked\n" +
                        "• Voice messages will be available while driving\n" +
                        "• Notifications appear in your car's display\n" +
                        "• Hands-free operation for safety",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
