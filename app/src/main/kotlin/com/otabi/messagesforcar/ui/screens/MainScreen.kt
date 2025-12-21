package com.otabi.messagesforcar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otabi.messagesforcar.automotive.AutomotiveHelper
import com.otabi.messagesforcar.automotive.DrivingStateManager
import com.otabi.messagesforcar.ui.components.WebViewContainer
import com.otabi.messagesforcar.ui.components.VoiceInputButton
import com.otabi.messagesforcar.voice.VoiceManager

/**
 * Main screen with WebView and driving-safe overlays.
 * Provides automotive-optimized UI shell around Messages for Web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onPairingStateChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    val isAutomotive = remember { AutomotiveHelper.isAutomotiveOS(context) }

    val drivingStateManager = remember { DrivingStateManager(context) }
    val isDriving by drivingStateManager.isDriving.collectAsState()

    var injectText by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    val voiceManager = remember {
        VoiceManager(
            context = context,
            onResult = { result ->
                if (!isDriving) {
                    injectText = result
                }
                isListening = false
            },
            onError = { error ->
                // TODO: Show error snackbar
                isListening = false
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
            drivingStateManager.destroy()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // WebView as the main content
        WebViewContainer(
            modifier = Modifier.fillMaxSize(),
            onPairingStateChanged = onPairingStateChanged,
            injectText = injectText,
            isDriving = isDriving
        )

        // Status bar overlay
        if (isAutomotive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Messages for Car",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = if (isDriving) "🚗 Driving Mode" else "🅿️ Parked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Voice input overlay
        if (!isDriving) { // Only show when not driving for safety
            VoiceInputButton(
                isListening = isListening,
                onClick = {
                    if (isListening) {
                        voiceManager.stopListening()
                        isListening = false
                    } else {
                        voiceManager.startListening()
                        isListening = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }

        // Driving mode overlay
        if (isDriving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "🚗 Driving Mode Active",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = "Voice input and messaging are disabled while driving for safety. Please pull over to send messages.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}