package com.scerruti.messagesforcar.ui.qr

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages pairing state for Google Messages Web integration.
 * Handles pairing detection, state persistence, and automotive-specific pairing logic.
 */
class PairingStateManager(private val context: Context) {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        "messages_pairing", Context.MODE_PRIVATE
    )
    
    private val _pairingState = MutableStateFlow(PairingState.UNKNOWN)
    val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()
    
    private val _isCheckingPairing = MutableStateFlow(false)
    val isCheckingPairing: StateFlow<Boolean> = _isCheckingPairing.asStateFlow()
    
    private val _lastPairingCheck = MutableStateFlow(0L)
    val lastPairingCheck: StateFlow<Long> = _lastPairingCheck.asStateFlow()
    
    init {
        // Load saved pairing state
        val savedState = preferences.getString(PREF_PAIRING_STATE, PairingState.UNKNOWN.name)
        _pairingState.value = PairingState.valueOf(savedState ?: PairingState.UNKNOWN.name)
        
        Log.d(TAG, "PairingStateManager initialized with state: ${_pairingState.value}")
    }
    
    /**
     * Update the pairing state and persist it.
     */
    fun updatePairingState(newState: PairingState) {
        val oldState = _pairingState.value
        if (oldState != newState) {
            _pairingState.value = newState
            _lastPairingCheck.value = System.currentTimeMillis()
            
            // Persist the state
            preferences.edit()
                .putString(PREF_PAIRING_STATE, newState.name)
                .putLong(PREF_LAST_CHECK, _lastPairingCheck.value)
                .apply()
            
            Log.d(TAG, "Pairing state changed from $oldState to $newState")
        }
    }
    
    /**
     * Check if the device is currently paired with Google Messages.
     * This is a simplified check for the native implementation.
     * In a real implementation, this would check with Google Messages API.
     */
    suspend fun checkPairingStatus(): PairingState {
        _isCheckingPairing.value = true
        
        try {
            // Simulate pairing check - in real implementation this would:
            // 1. Check Google Messages Web session
            // 2. Verify active connection
            // 3. Test message sync capability
            delay(1000) // Simulate network check
            
            // For now, we'll use a simple heuristic based on time since last successful pairing
            val lastSuccessfulPairing = preferences.getLong(PREF_LAST_SUCCESSFUL_PAIRING, 0L)
            val timeSinceLastPairing = System.currentTimeMillis() - lastSuccessfulPairing
            
            val currentState = when {
                timeSinceLastPairing < PAIRING_VALIDITY_DURATION -> PairingState.PAIRED
                lastSuccessfulPairing > 0 -> PairingState.EXPIRED
                else -> PairingState.UNPAIRED
            }
            
            updatePairingState(currentState)
            return currentState
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check pairing status", e)
            updatePairingState(PairingState.ERROR)
            return PairingState.ERROR
        } finally {
            _isCheckingPairing.value = false
        }
    }
    
    /**
     * Mark pairing as successful.
     * Called when QR code pairing is completed successfully.
     */
    fun markPairingSuccessful() {
        preferences.edit()
            .putLong(PREF_LAST_SUCCESSFUL_PAIRING, System.currentTimeMillis())
            .apply()
        
        updatePairingState(PairingState.PAIRED)
        Log.d(TAG, "Pairing marked as successful")
    }
    
    /**
     * Reset pairing state.
     * Forces user to go through pairing process again.
     */
    fun resetPairing() {
        preferences.edit()
            .remove(PREF_PAIRING_STATE)
            .remove(PREF_LAST_SUCCESSFUL_PAIRING)
            .remove(PREF_LAST_CHECK)
            .apply()
        
        updatePairingState(PairingState.UNKNOWN)
        Log.d(TAG, "Pairing state reset")
    }
    
    /**
     * Get a user-friendly description of the current pairing state.
     */
    fun getPairingStateDescription(): String {
        return when (_pairingState.value) {
            PairingState.UNKNOWN -> "Checking pairing status..."
            PairingState.UNPAIRED -> "Not paired with Google Messages"
            PairingState.PAIRED -> "Connected to Google Messages"
            PairingState.EXPIRED -> "Pairing expired, please reconnect"
            PairingState.ERROR -> "Error checking pairing status"
        }
    }
    
    /**
     * Check if QR code should be shown based on current state.
     */
    fun shouldShowQRCode(): Boolean {
        return when (_pairingState.value) {
            PairingState.UNPAIRED, PairingState.EXPIRED, PairingState.ERROR -> true
            PairingState.PAIRED -> false
            PairingState.UNKNOWN -> true // Show QR while checking
        }
    }
    
    companion object {
        private const val TAG = "PairingStateManager"
        private const val PREF_PAIRING_STATE = "pairing_state"
        private const val PREF_LAST_SUCCESSFUL_PAIRING = "last_successful_pairing"
        private const val PREF_LAST_CHECK = "last_pairing_check"
        
        // Pairing is considered valid for 7 days
        private const val PAIRING_VALIDITY_DURATION = 7 * 24 * 60 * 60 * 1000L
    }
}

/**
 * Represents the current pairing state with Google Messages.
 */
enum class PairingState {
    UNKNOWN,    // Initial state, pairing status not yet determined
    UNPAIRED,   // Not paired with Google Messages
    PAIRED,     // Successfully paired and connected
    EXPIRED,    // Previously paired but connection expired
    ERROR       // Error occurred while checking pairing status
}

/**
 * Composable that provides access to PairingStateManager.
 * Use this to get pairing state in Compose UI.
 */
@Composable
fun rememberPairingStateManager(context: Context): PairingStateManager {
    return remember { PairingStateManager(context) }
}

/**
 * Composable hook for observing pairing state.
 */
@Composable
fun PairingStateManager.collectPairingStateAsState(): State<PairingState> {
    return pairingState.collectAsState()
}

/**
 * Composable hook for observing pairing check status.
 */
@Composable
fun PairingStateManager.collectIsCheckingPairingAsState(): State<Boolean> {
    return isCheckingPairing.collectAsState()
}
