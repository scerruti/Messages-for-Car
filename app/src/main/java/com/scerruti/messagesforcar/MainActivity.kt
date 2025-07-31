package com.scerruti.messagesforcar

import android.Manifest
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
import com.scerruti.messagesforcar.service.MessagesBackgroundService
import com.scerruti.messagesforcar.ui.theme.MessagesForCarTheme
import com.scerruti.messagesforcar.ui.MessagingWebView

class MainActivity : ComponentActivity() {
    
    private var hasNotificationPermission by mutableStateOf(false)
    private var backgroundServiceRunning by mutableStateOf(false)
    private var isPaired by mutableStateOf(false)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasNotificationPermission = isGranted
        // Don't automatically start service - wait for user to pair first
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
        // Don't automatically start background service - wait for pairing
    }
    
    private fun startBackgroundService() {
        MessagesBackgroundService.startService(this)
        backgroundServiceRunning = true
    }
    
    private fun onPairingStateChanged(paired: Boolean) {
        isPaired = paired
        if (paired && hasNotificationPermission && !backgroundServiceRunning) {
            // Automatically start background service when paired
            startBackgroundService()
        }
    }
    
    @Composable
    private fun MainContent(modifier: Modifier = Modifier) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Status bar showing service state
            StatusBar()
            
            // Main WebView content
            MessagingWebView(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                onPairingStateChanged = ::onPairingStateChanged
            )
        }
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
                containerColor = if (backgroundServiceRunning) 
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
                            !backgroundServiceRunning -> "🟡 Paired - Starting Service..."
                            backgroundServiceRunning -> "🟢 Background Service Active"
                            else -> "🔴 Service Stopped"
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
                
                if (backgroundServiceRunning) {
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
                        text = "Phone paired! Background monitoring will start automatically",
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
        // Note: We don't stop the background service here as it should continue running
        // even when the main UI is closed (for automotive use cases)
    }
}
