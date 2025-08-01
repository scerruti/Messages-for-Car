package com.scerruti.messagesforcar.sync

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.work.*
import com.scerruti.messagesforcar.automotive.AutomotiveHelper
import com.scerruti.messagesforcar.data.preferences.PairingPreferences
import java.util.concurrent.TimeUnit

/**
 * Manages background message synchronization using WorkManager.
 * Provides automotive-optimized sync scheduling and power management.
 */
class MessageSyncManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "MessageSyncManager"
        
        @Volatile
        private var INSTANCE: MessageSyncManager? = null
        
        fun getInstance(context: Context): MessageSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MessageSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val workManager = WorkManager.getInstance(context)
    private val pairingPreferences = PairingPreferences.getInstance(context)
    
    /**
     * Start background message synchronization
     */
    fun startSync() {
        if (!pairingPreferences.isPaired) {
            Log.w(TAG, "Cannot start sync - device not paired")
            return
        }
        
        // Configure sync parameters based on automotive context
        val isAutomotive = AutomotiveHelper.isAutomotiveOS(context)
        
        if (isAutomotive) {
            Log.d(TAG, "Starting automotive-optimized message sync")
            startAutomotiveOptimizedSync()
        } else {
            Log.d(TAG, "Starting standard message sync")
            startStandardSync()
        }
    }
    
    /**
     * Stop background message synchronization
     */
    fun stopSync() {
        MessageSyncWorker.cancelSync(context)
        Log.d(TAG, "Stopped message sync")
    }
    
    /**
     * Trigger immediate sync (e.g., when user opens app)
     */
    fun triggerImmediateSync() {
        if (!pairingPreferences.isPaired) {
            Log.w(TAG, "Cannot trigger sync - device not paired")
            return
        }
        
        MessageSyncWorker.triggerImmediateSync(context)
        Log.d(TAG, "Triggered immediate sync")
    }
    
    /**
     * Check if sync is currently running
     */
    fun isSyncRunning(): LiveData<Boolean> {
        return workManager.getWorkInfosForUniqueWorkLiveData(MessageSyncWorker.WORK_NAME)
            .map { workInfos ->
                workInfos?.any { workInfo ->
                    workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED
                } ?: false
            }
    }
    
    /**
     * Get sync work status for debugging/monitoring
     */
    fun getSyncStatus(): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(MessageSyncWorker.WORK_NAME)
    }
    
    /**
     * Handle pairing state changes
     */
    fun onPairingStateChanged(isPaired: Boolean) {
        if (isPaired) {
            Log.d(TAG, "Device paired - starting sync")
            startSync()
        } else {
            Log.d(TAG, "Device unpaired - stopping sync")
            stopSync()
        }
    }
    
    /**
     * Start automotive-optimized sync with conservative power usage
     */
    private fun startAutomotiveOptimizedSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false) // Don't wait for idle in automotive
            .build()

        // Use longer intervals for automotive to conserve power
        val periodicWorkRequest = PeriodicWorkRequestBuilder<MessageSyncWorker>(
            30, TimeUnit.MINUTES // 30 minutes for automotive
        )
            .setConstraints(constraints)
            .addTag(MessageSyncWorker.TAG)
            .addTag("automotive")
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            MessageSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
        
        Log.d(TAG, "Started automotive-optimized sync (30 min intervals)")
    }
    
    /**
     * Start standard sync for non-automotive devices
     */
    private fun startStandardSync() {
        MessageSyncWorker.schedulePeriodicSync(context)
        Log.d(TAG, "Started standard sync (15 min intervals)")
    }
    
    /**
     * Handle network connectivity changes
     */
    fun onNetworkConnectivityChanged(isConnected: Boolean) {
        if (isConnected && pairingPreferences.isPaired) {
            Log.d(TAG, "Network connected - triggering immediate sync")
            triggerImmediateSync()
        } else if (!isConnected) {
            Log.d(TAG, "Network disconnected - sync will resume when connected")
        }
    }
    
    /**
     * Handle automotive power state changes
     */
    fun onPowerStateChanged(isLowPower: Boolean) {
        val isAutomotive = AutomotiveHelper.isAutomotiveOS(context)
        
        if (isAutomotive && isLowPower) {
            Log.d(TAG, "Low power state - temporarily stopping sync")
            stopSync()
        } else if (isAutomotive && !isLowPower && pairingPreferences.isPaired) {
            Log.d(TAG, "Normal power state - resuming sync")
            startSync()
        }
    }
}

/**
 * Extension function to map LiveData
 */
private fun <T, R> LiveData<T>.map(mapper: (T) -> R): LiveData<R> {
    val result = androidx.lifecycle.MediatorLiveData<R>()
    result.addSource(this) { value ->
        result.value = mapper(value)
    }
    return result
}
