package com.scerruti.messagesforcar.service

import android.app.IntentService
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.scerruti.messagesforcar.R

/**
 * Service that handles reply and mark-as-read actions for message notifications.
 * This integrates with Android Automotive's messaging system.
 */
class MessageActionService : IntentService("MessageActionService") {
    
    companion object {
        private const val TAG = "MessageActionService"
        
        // Action constants
        private const val ACTION_REPLY = "com.scerruti.messagesforcar.REPLY"
        private const val ACTION_MARK_AS_READ = "com.scerruti.messagesforcar.MARK_AS_READ"
        
        // Extra keys
        private const val EXTRA_SENDER = "sender"
        private const val EXTRA_MESSAGE_ID = "message_id"
        private const val REMOTE_INPUT_RESULT_KEY = "reply_input"
        
        /**
         * Creates a reply action for message notifications
         */
        fun createReplyAction(context: Context, sender: String, messageId: Int): NotificationCompat.Action {
            val replyIntent = createReplyIntent(context, sender, messageId)
            val replyPendingIntent = PendingIntent.getService(
                context,
                messageId * 2, // Unique ID for reply actions
                replyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            
            val remoteInput = RemoteInput.Builder(REMOTE_INPUT_RESULT_KEY)
                .setLabel("Reply...")
                .build()
            
            return NotificationCompat.Action.Builder(
                R.drawable.ic_reply,
                "Reply",
                replyPendingIntent
            )
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                .setShowsUserInterface(false)
                .addRemoteInput(remoteInput)
                .build()
        }
        
        /**
         * Creates a mark-as-read action for message notifications
         */
        fun createMarkAsReadAction(context: Context, sender: String, messageId: Int): NotificationCompat.Action {
            val markAsReadIntent = createMarkAsReadIntent(context, sender, messageId)
            val markAsReadPendingIntent = PendingIntent.getService(
                context,
                messageId * 2 + 1, // Unique ID for mark-as-read actions
                markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            return NotificationCompat.Action.Builder(
                R.drawable.ic_mark_read,
                "Mark as Read",
                markAsReadPendingIntent
            )
                .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_MARK_AS_READ)
                .setShowsUserInterface(false)
                .build()
        }
        
        private fun createReplyIntent(context: Context, sender: String, messageId: Int): Intent {
            return Intent(context, MessageActionService::class.java).apply {
                action = ACTION_REPLY
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_MESSAGE_ID, messageId)
            }
        }
        
        private fun createMarkAsReadIntent(context: Context, sender: String, messageId: Int): Intent {
            return Intent(context, MessageActionService::class.java).apply {
                action = ACTION_MARK_AS_READ
                putExtra(EXTRA_SENDER, sender)
                putExtra(EXTRA_MESSAGE_ID, messageId)
            }
        }
    }
    
    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) return
        
        val sender = intent.getStringExtra(EXTRA_SENDER) ?: return
        val messageId = intent.getIntExtra(EXTRA_MESSAGE_ID, -1)
        
        Log.d(TAG, "Handling action: ${intent.action} for sender: $sender")
        
        when (intent.action) {
            ACTION_REPLY -> handleReply(intent, sender, messageId)
            ACTION_MARK_AS_READ -> handleMarkAsRead(sender, messageId)
        }
    }
    
    private fun handleReply(intent: Intent, sender: String, messageId: Int) {
        // Extract reply text from RemoteInput
        val results: Bundle = RemoteInput.getResultsFromIntent(intent) ?: return
        val replyText = results.getString(REMOTE_INPUT_RESULT_KEY)?.trim()
        
        if (replyText.isNullOrEmpty()) {
            Log.w(TAG, "Reply text is empty")
            return
        }
        
        Log.d(TAG, "Replying to $sender: $replyText")
        
        // Send reply through the background WebView service
        sendReplyToWebView(sender, replyText)
        
        // Optionally, you could show a confirmation notification
        // showReplyConfirmation(sender, replyText)
    }
    
    private fun handleMarkAsRead(sender: String, messageId: Int) {
        Log.d(TAG, "Marking conversation with $sender as read")
        
        // Send mark-as-read command to the background WebView service
        markConversationAsRead(sender)
    }
    
    private fun sendReplyToWebView(sender: String, replyText: String) {
        // Send intent to background service to execute reply
        val intent = Intent(this, MessagesBackgroundService::class.java).apply {
            action = "SEND_REPLY"
            putExtra("sender", sender)
            putExtra("reply_text", replyText)
        }
        startService(intent)
        
        // For now, log the action
        Log.d(TAG, "Reply sent to WebView service - Sender: $sender, Reply: $replyText")
    }
    
    private fun markConversationAsRead(sender: String) {
        // Send intent to background service to mark as read
        val intent = Intent(this, MessagesBackgroundService::class.java).apply {
            action = "MARK_AS_READ"
            putExtra("sender", sender)
        }
        startService(intent)
        
        // For now, log the action
        Log.d(TAG, "Mark as read sent to WebView service - Sender: $sender")
    }
    
    private fun showReplyConfirmation(sender: String, replyText: String) {
        // Could show a brief confirmation that the reply was sent
        // This is optional and might not be needed for automotive use
    }
}
