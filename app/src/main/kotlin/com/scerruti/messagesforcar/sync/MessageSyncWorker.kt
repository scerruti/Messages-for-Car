package com.scerruti.messagesforcar.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.scerruti.messagesforcar.data.repository.MessageRepository
import com.scerruti.messagesforcar.data.repository.ConversationRepository
import com.scerruti.messagesforcar.data.preferences.PairingPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker for background message synchronization.
 * Syncs messages from Google Messages for Web while respecting automotive power constraints.
 */
class MessageSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG = "MessageSyncWorker"
        const val WORK_NAME = "message_sync_work"
        
        /**
         * Schedule periodic message synchronization
         */
        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true) // Automotive battery optimization
                .build()

            val periodicWorkRequest = PeriodicWorkRequestBuilder<MessageSyncWorker>(
                15, TimeUnit.MINUTES // Sync every 15 minutes
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicWorkRequest
                )
                
            Log.d(TAG, "Scheduled periodic message sync work")
        }
        
        /**
         * Cancel all sync work
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "Cancelled message sync work")
        }
        
        /**
         * Trigger immediate one-time sync
         */
        fun triggerImmediateSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val oneTimeWorkRequest = OneTimeWorkRequestBuilder<MessageSyncWorker>()
                .setConstraints(constraints)
                .addTag("immediate_sync")
                .build()

            WorkManager.getInstance(context).enqueue(oneTimeWorkRequest)
            Log.d(TAG, "Triggered immediate message sync")
        }
    }

    private lateinit var messageRepository: MessageRepository
    private lateinit var conversationRepository: ConversationRepository
    private lateinit var pairingPreferences: PairingPreferences

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting message sync work")
            
            // Initialize dependencies
            messageRepository = MessageRepository.getInstance(applicationContext)
            conversationRepository = ConversationRepository.getInstance(applicationContext)
            pairingPreferences = PairingPreferences.getInstance(applicationContext)
            
            // Check if device is paired
            if (!pairingPreferences.isPaired) {
                Log.w(TAG, "Device not paired, skipping sync")
                return@withContext Result.success()
            }
            
            // Check if pairing is recent (to avoid stale sessions)
            if (!pairingPreferences.isPairingRecent()) {
                Log.w(TAG, "Pairing is not recent, may need re-pairing")
                // Could trigger a notification to re-pair, but for now just continue
            }
            
            // Perform the actual sync
            val syncResult = performMessageSync()
            
            if (syncResult.isSuccess) {
                Log.d(TAG, "Message sync completed successfully")
                Result.success()
            } else {
                Log.e(TAG, "Message sync failed: ${syncResult.errorMessage}")
                // Return retry for transient failures, failure for permanent issues
                if (syncResult.isRetryable) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception during message sync", e)
            Result.retry()
        }
    }
    
    /**
     * Performs the actual message synchronization logic
     */
    private suspend fun performMessageSync(): SyncResult {
        return try {
            // This is where we would implement the actual sync logic
            // For now, we'll create a placeholder that simulates sync
            
            Log.d(TAG, "Performing message sync...")
            
            // Simulate checking for new messages
            // In a real implementation, this would:
            // 1. Connect to Google Messages for Web API (if available)
            // 2. Or parse WebView content for new messages
            // 3. Store new messages in Room database
            // 4. Update conversation metadata
            // 5. Trigger notifications for new messages
            
            // Placeholder: Log current database state
            val conversationCount = conversationRepository.getAllConversationsSync().size
            val messageCount = messageRepository.getAllMessagesSync().size
            
            Log.d(TAG, "Current state: $conversationCount conversations, $messageCount messages")
            
            // Simulate successful sync
            SyncResult.success()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during message sync", e)
            SyncResult.failure(e.message ?: "Unknown sync error", isRetryable = true)
        }
    }
    
    /**
     * Result of a sync operation
     */
    data class SyncResult(
        val isSuccess: Boolean,
        val errorMessage: String? = null,
        val isRetryable: Boolean = false
    ) {
        companion object {
            fun success() = SyncResult(isSuccess = true)
            fun failure(message: String, isRetryable: Boolean = false) = 
                SyncResult(isSuccess = false, errorMessage = message, isRetryable = isRetryable)
        }
    }
}
