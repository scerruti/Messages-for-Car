package com.otabi.messagesforcar.ui.signin

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.otabi.messagesforcar.service.MessagesBackgroundService
import com.otabi.messagesforcar.ui.theme.MessagesForCarTheme
import com.otabi.messagesforcar.data.preferences.PairingPreferences
import kotlinx.coroutines.launch

/**
 * Account sign-in based pairing activity.
 * Loads Google Messages for Web and guides user through account-based device pairing.
 */
class SignInPairingActivity : ComponentActivity() {
    
    private lateinit var pairingStateManager: PairingStateManager
    private lateinit var pairingPreferences: PairingPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable WebView debugging for Messages for Web troubleshooting
        WebView.setWebContentsDebuggingEnabled(true)

        // Isolate sign-in pairing debug session to a single foreground WebView.
        // The background service owns a hidden WebView and can create extra Chrome inspect targets.
        MessagesBackgroundService.stopService(this)
        
        pairingStateManager = PairingStateManager(this)
        pairingPreferences = PairingPreferences.getInstance(this)
        
        setContent {
            MessagesForCarTheme {
                SignInPairingActivityContent(
                    pairingStateManager = pairingStateManager,
                    onPairingComplete = { finishPairingFlow() },
                    onCancel = { finish() }
                )
            }
        }

        // Pairing activity should always start in manual pairing mode.
        // Avoid heuristic auto-success states that can close the flow incorrectly.
        pairingStateManager.updatePairingState(PairingState.UNPAIRED)
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
private fun SignInPairingActivityContent(
    pairingStateManager: PairingStateManager,
    onPairingComplete: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val pairingState by pairingStateManager.collectPairingStateAsState()
    val isCheckingPairing by pairingStateManager.collectIsCheckingPairingAsState()
    val scrollState = rememberScrollState()
    
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
                            imageVector = Icons.Default.ArrowBack,
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
                    
                    else -> {
                        // Sign-in pairing screen
                        PairingAuthScreen(
                            modifier = Modifier.fillMaxWidth(),
                            onPairingComplete = onPairingComplete,
                            onRetry = {
                                errorMessage = null
                            },
                            isLoading = isCheckingPairing,
                            errorMessage = errorMessage
                        )
                    }
                }
            }
            
            // Floating action for manual pairing check
            FloatingActionButton(
                onClick = {
                    // Could trigger a refresh if needed
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
                        imageVector = Icons.Default.Refresh,
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
                    imageVector = Icons.Default.CheckCircle,
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
