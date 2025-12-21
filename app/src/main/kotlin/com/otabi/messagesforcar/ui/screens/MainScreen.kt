package com.otabi.messagesforcar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.otabi.messagesforcar.automotive.AutomotiveHelper
import com.otabi.messagesforcar.ui.components.WebViewContainer

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
    // TODO: Add driving state detection when available
    val isDriving = remember { false } // Placeholder for driving state

    Box(modifier = modifier.fillMaxSize()) {
        // WebView as the main content
        WebViewContainer(
            modifier = Modifier.fillMaxSize(),
            onPairingStateChanged = onPairingStateChanged
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

        // Voice input overlay (placeholder for Step 5)
        if (!isDriving) { // Only show when not driving for safety
            FloatingActionButton(
                onClick = { /* TODO: Implement voice input */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("🎤", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}