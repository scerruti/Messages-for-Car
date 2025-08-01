package com.scerruti.messagesforcar.ui.qr

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Native QR Code Pairing Screen using ZXing library.
 * Replaces unreliable WebView QR code implementation with automotive-optimized native UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodePairingScreen(
    modifier: Modifier = Modifier,
    onPairingComplete: () -> Unit = {},
    onRetry: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(true) }
    var generationError by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    // Generate QR code on first composition
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
                text = "Scan this QR code with Google Messages on your phone",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp // Automotive-optimized size
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // QR Code Display Area
            Card(
                modifier = Modifier.size(320.dp), // Large size for automotive visibility
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        isGenerating -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Generating QR Code...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        generationError != null || errorMessage != null -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Error",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = generationError ?: errorMessage ?: "Unknown error",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                        
                        qrCodeBitmap != null -> {
                            Image(
                                bitmap = qrCodeBitmap!!.asImageBitmap(),
                                contentDescription = "QR Code for pairing with Google Messages",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        else -> {
                            Text(
                                text = "No QR code available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    
                    val instructions = listOf(
                        "1. Open Google Messages on your phone",
                        "2. Tap the menu (three lines) in the top left",
                        "3. Select 'Messages for web'",
                        "4. Tap 'QR code scanner'",
                        "5. Point your phone camera at this QR code"
                    )
                    
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
                        text = "Skip for Now",
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
        canvas.drawColor(androidx.compose.ui.graphics.Color.White.toArgb())
        
        // Draw QR code pixels
        paint.color = androidx.compose.ui.graphics.Color.Black.toArgb()
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
