package com.otabi.messagesforcar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otabi.messagesforcar.automotive.AutomotiveHelper
import com.otabi.messagesforcar.pairing.PairingState
import com.otabi.messagesforcar.ui.components.WebViewContainer
import com.otabi.messagesforcar.voice.VoiceManager
import kotlinx.coroutines.delay

/**
 * Main screen with WebView and driving-safe overlays.
 * Provides automotive-optimized UI shell around Messages for Web.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onPairingStateChanged: ((PairingState) -> Unit)? = null
) {
    val context = LocalContext.current
    val isAutomotive = remember { AutomotiveHelper.isAutomotiveOS(context) }

    var pairingState by remember { mutableStateOf(PairingState.UNKNOWN) }
    var injectText by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var autoSend by remember { mutableStateOf(false) }
    var pendingSendText by remember { mutableStateOf<String?>(null) }
    var countdown by remember { mutableStateOf(0) }
    var isCountdownActive by remember { mutableStateOf(false) }

    val voiceManager = remember(context) {
        VoiceManager(
            context = context,
            onResult = { result ->
                pendingSendText = result
                countdown = 3
                isCountdownActive = true
                isListening = false
            },
            onError = {
                isListening = false
            }
        )
    }


    DisposableEffect(Unit) {
        onDispose {
            voiceManager.destroy()
        }
    }

    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            while (countdown > 0) {
                delay(1000)
                countdown -= 1
            }
            if (countdown == 0 && pendingSendText != null) {
                injectText = pendingSendText
                pendingSendText = null
                isCountdownActive = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Onboarding UI
        if (pairingState == PairingState.QR_REQUIRED) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Scan the QR code with your phone to pair.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else if (pairingState == PairingState.IMAGE_MATCH_REQUIRED) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = "Match the icon shown here with the one on your phone to pair.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        }

        // WebView as the main content
        WebViewContainer(
            modifier = Modifier.fillMaxSize(),
            onPairingStateChanged = { pairingState = it },
            injectText = injectText,
            onTextInjected = { injectText = null },
            autoSend = autoSend
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
                        text = "Messages for Car",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Settings icon
        IconButton(
            onClick = { showSettings = !showSettings },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Settings dropdown menu
        DropdownMenu(
            expanded = showSettings,
            onDismissRequest = { showSettings = false }
        ) {
            DropdownMenuItem(
                text = { Text("Auto-send voice messages") },
                trailingIcon = {
                    Switch(
                        checked = autoSend,
                        onCheckedChange = { autoSend = it }
                    )
                },
                onClick = { }
            )
        }

        // Voice input overlay
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

        // Countdown overlay
        if (isCountdownActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sending in $countdown…",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(
                            onClick = {
                                pendingSendText = null
                                isCountdownActive = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}