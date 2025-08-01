package com.scerruti.messagesforcar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.scerruti.messagesforcar.automotive.AutomotiveHelper
import com.scerruti.messagesforcar.sync.MessageSyncManager
import com.scerruti.messagesforcar.ui.theme.MessagesForCarTheme
import com.scerruti.messagesforcar.ui.MessagingWebView
import com.scerruti.messagesforcar.ui.qr.QRCodePairingActivity
import com.scerruti.messagesforcar.data.preferences.PairingPreferences

class MainActivity : ComponentActivity() {
    
    private var hasNotificationPermission by mutableStateOf(false)
    private var syncServiceRunning by mutableStateOf(false)
    private var isPaired by mutableStateOf(false)
    private lateinit var pairingPreferences: PairingPreferences
    private lateinit var syncManager: MessageSyncManager
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
        // Don't automatically start service - wait for user to pair first
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize pairing preferences and sync manager
        pairingPreferences = PairingPreferences.getInstance(this)
        syncManager = MessageSyncManager.getInstance(this)
        
        // Log automotive information for debugging
        AutomotiveHelper.logAutomotiveInfo(this)
        
        checkAndRequestPermissions()
        
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
        if (isPaired && hasNotificationPermission && !syncServiceRunning) {
            startSyncService()
        }
    }
    
    private fun checkAndRequestPermissions() {
        hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Notifications don't need permission on older versions
        }
        
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Don't automatically start sync service - wait for pairing
    }
    
    private fun startSyncService() {
        syncManager.startSync()
        syncServiceRunning = true
    }
    
    private fun onPairingStateChanged(paired: Boolean) {
        isPaired = paired
        if (paired && hasNotificationPermission && !syncServiceRunning) {
            // Automatically start sync service when paired
            startSyncService()
        } else if (!paired && syncServiceRunning) {
            // Stop sync when unpaired
            syncManager.stopSync()
            syncServiceRunning = false
        }
    }
    
    @Composable
    private fun MainContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Status bar showing service state
            StatusBar()
            
            if (!isPaired) {
                // Show QR pairing prompt instead of WebView when not paired
                QRPairingPrompt(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                // Main WebView content - only show when paired
                MessagingWebView(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    onPairingStateChanged = ::onPairingStateChanged
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
    
    private fun openQRCodePairing() {
        val intent = Intent(this, QRCodePairingActivity::class.java)
        startActivity(intent)
    }
    
    @Composable
    private fun StatusBar() {
        val isAutomotive = AutomotiveHelper.isAutomotiveOS(this)
        val isDriving = AutomotiveHelper.isDriving(this)
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (syncServiceRunning) 
                    MaterialTheme.colorScheme.primaryContainer 
                else 
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Messages for Car",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                
                if (isAutomotive) {
                    Text(
                        text = "🚗 Android Automotive OS",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when {
                            !isPaired -> "📱 Scan QR Code to Pair"
                            !syncServiceRunning -> "🟡 Paired - Starting Sync..."
                            syncServiceRunning -> "🟢 Background Sync Active"
                            else -> "🔴 Sync Stopped"
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (!hasNotificationPermission) {
                        Text(
                            text = "⚠️ Notifications Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (syncServiceRunning) {
                    val notificationText = if (isAutomotive) {
                        "Messages will appear as automotive notifications with voice reply"
                    } else {
                        "Messages will appear as Android notifications"
                    }
                    
                    Text(
                        text = notificationText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (!isPaired) {
                    Text(
                        text = "Scan the QR code below with your phone's Google Messages app",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Phone paired! Background sync will start automatically",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Note: We don't stop the sync service here as it should continue running
        // even when the main UI is closed (for automotive use cases)
        // WorkManager handles its own lifecycle
    }
}
