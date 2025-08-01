package com.scerruti.messagesforcar.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Manages pairing state preferences for the Messages for Car app.
 * Provides a simple API for storing and retrieving pairing status.
 */
class PairingPreferences private constructor(private val prefs: SharedPreferences) {
    
    companion object {
        private const val PREFS_NAME = "pairing_preferences"
        private const val KEY_IS_PAIRED = "is_paired"
        private const val KEY_PAIRING_URL = "pairing_url"
        private const val KEY_PAIRING_TIMESTAMP = "pairing_timestamp"
        
        @Volatile
        private var INSTANCE: PairingPreferences? = null
        
        fun getInstance(context: Context): PairingPreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PairingPreferences(
                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                ).also { INSTANCE = it }
            }
        }
    }
    
    /**
     * Check if the device is currently paired with Google Messages for Web
     */
    var isPaired: Boolean
        get() = prefs.getBoolean(KEY_IS_PAIRED, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_PAIRED, value) }
    
    /**
     * Get the last pairing URL used for QR code generation
     */
    var pairingUrl: String?
        get() = prefs.getString(KEY_PAIRING_URL, null)
        set(value) = prefs.edit { putString(KEY_PAIRING_URL, value) }
    
    /**
     * Get the timestamp when pairing was last established
     */
    var pairingTimestamp: Long
        get() = prefs.getLong(KEY_PAIRING_TIMESTAMP, 0L)
        set(value) = prefs.edit { putLong(KEY_PAIRING_TIMESTAMP, value) }
    
    /**
     * Mark the device as successfully paired
     */
    fun markAsPaired(url: String) {
        prefs.edit {
            putBoolean(KEY_IS_PAIRED, true)
            putString(KEY_PAIRING_URL, url)
            putLong(KEY_PAIRING_TIMESTAMP, System.currentTimeMillis())
        }
    }
    
    /**
     * Clear pairing information (e.g., when user logs out)
     */
    fun clearPairing() {
        prefs.edit {
            putBoolean(KEY_IS_PAIRED, false)
            remove(KEY_PAIRING_URL)
            remove(KEY_PAIRING_TIMESTAMP)
        }
    }
    
    /**
     * Check if pairing is recent (within 24 hours)
     * This can be used to determine if re-pairing might be needed
     */
    fun isPairingRecent(): Boolean {
        val twentyFourHoursAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return pairingTimestamp > twentyFourHoursAgo
    }
}
