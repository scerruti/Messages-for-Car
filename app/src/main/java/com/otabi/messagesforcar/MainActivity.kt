package com.otabi.messagesforcar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.otabi.messagesforcar.automotive.AutomotiveHelper
import com.otabi.messagesforcar.ui.theme.MessagesForCarTheme
import com.otabi.messagesforcar.ui.screens.MainScreen
import com.otabi.messagesforcar.data.preferences.PairingPreferences
import com.otabi.messagesforcar.ui.qr.QRCodePairingActivity

class MainActivity : ComponentActivity() {

    private lateinit var pairingPreferences: PairingPreferences
    private var isPaired by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize pairing preferences
        pairingPreferences = PairingPreferences.getInstance(this)
        isPaired = pairingPreferences.isPaired

        // Log automotive information for debugging
        AutomotiveHelper.logAutomotiveInfo(this)

        setContent {
            MessagesForCarTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainContent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check pairing state when activity resumes (e.g., returning from QR pairing)
        isPaired = pairingPreferences.isPaired
    }

    private fun onPairingStateChanged(paired: Boolean) {
        isPaired = paired
        if (paired) {
            pairingPreferences.markAsPaired("https://messages.google.com/web")
        }
    }

    private fun openQRCodePairing() {
        val intent = Intent(this, QRCodePairingActivity::class.java)
        startActivity(intent)
    }

    @Composable
    private fun MainContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            if (!isPaired) {
                // Show QR pairing prompt
                QRPairingPrompt(modifier = Modifier.fillMaxSize())
            } else {
                // Show main screen with WebView and overlays
                MainScreen(
                    modifier = Modifier.fillMaxSize(),
                    onPairingStateChanged = { paired -> onPairingStateChanged(paired) }
                )
            }
        }
    }

    @Composable
    private fun QRPairingPrompt(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔗 Pair Your Phone",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To start receiving messages in your car, you need to pair your phone with Google Messages for Web.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            ElevatedButton(
                onClick = { openQRCodePairing() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "Show QR Code",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Scan the QR code with your phone's Google Messages app to pair your devices.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
