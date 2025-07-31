package com.scerruti.messagesforcar.automotive

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Utility class for Android Automotive OS specific functionality
 */
object AutomotiveHelper {
    
    private const val TAG = "AutomotiveHelper"
    
    /**
     * Check if the app is running on Android Automotive OS
     */
    fun isAutomotiveOS(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
    }
    
    /**
     * Check if the device is currently in driving mode
     * This is a simplified implementation - real automotive systems would
     * integrate with car sensors and driving state APIs
     */
    fun isDriving(context: Context): Boolean {
        // For now, always assume driving mode when on automotive
        // In a real implementation, this would check:
        // - Car speed sensors
        // - Parking brake status
        // - Android Automotive driving state APIs
        return isAutomotiveOS(context)
    }
    
    /**
     * Get automotive-optimized notification priority
     */
    fun getNotificationPriority(isUrgent: Boolean = false): Int {
        return if (isUrgent) {
            // High priority for urgent messages (emergency contacts, etc.)
            android.app.Notification.PRIORITY_HIGH
        } else {
            // Default priority for regular messages
            android.app.Notification.PRIORITY_DEFAULT
        }
    }
    
    /**
     * Check if voice replies are available
     */
    fun isVoiceReplyAvailable(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }
    
    /**
     * Log automotive-specific information for debugging
     */
    fun logAutomotiveInfo(context: Context) {
        Log.d(TAG, "=== Automotive Info ===")
        Log.d(TAG, "Is Automotive OS: ${isAutomotiveOS(context)}")
        Log.d(TAG, "Is Driving: ${isDriving(context)}")
        Log.d(TAG, "Voice Reply Available: ${isVoiceReplyAvailable(context)}")
        Log.d(TAG, "Package: ${context.packageName}")
        Log.d(TAG, "======================")
    }
}
