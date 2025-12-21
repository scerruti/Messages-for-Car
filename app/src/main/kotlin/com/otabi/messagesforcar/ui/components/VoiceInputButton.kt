package com.otabi.messagesforcar.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Voice input button for automotive messaging.
 * Provides visual feedback for speech recognition state.
 */
@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(72.dp), // Larger for automotive touch
        containerColor = if (isListening)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = if (isListening) "🎙️" else "🎤",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}