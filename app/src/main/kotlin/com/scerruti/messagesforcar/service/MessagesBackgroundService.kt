package com.scerruti.messagesforcar.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.webkit.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.scerruti.messagesforcar.R

/**
 * Background service that maintains a hidden WebView connection to Google Messages
 * and intercepts new messages to create Android Automotive notifications.
 */
class MessagesBackgroundService : Service() {
    
    companion object {
        private const val TAG = "MessagesBackgroundService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "messages_background_service"
        private const val MESSAGE_CHANNEL_ID = "messages_notifications"
        
        fun startService(context: Context) {
            val intent = Intent(context, MessagesBackgroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, MessagesBackgroundService::class.java)
            context.stopService(intent)
        }
    }
    
    private lateinit var webView: WebView
    private lateinit var notificationManager: NotificationManagerCompat
    private var messageCounter = 0
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Background service created")
        
        createNotificationChannels()
        notificationManager = NotificationManagerCompat.from(this)
        
        setupWebView()
        startForeground(NOTIFICATION_ID, createServiceNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started with action: ${intent?.action}")
        
        when (intent?.action) {
            "SEND_REPLY" -> {
                val sender = intent.getStringExtra("sender")
                val replyText = intent.getStringExtra("reply_text")
                if (sender != null && replyText != null) {
                    sendReplyMessage(sender, replyText)
                }
            }
            "MARK_AS_READ" -> {
                val sender = intent.getStringExtra("sender")
                if (sender != null) {
                    markConversationAsRead(sender)
                }
            }
            else -> {
                // Initial startup - load Google Messages for Web
                webView.loadUrl("https://messages.google.com/web")
            }
        }
        
        return START_STICKY // Restart service if killed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Background service destroyed")
        
        try {
            webView.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying WebView", e)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Messages Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Messages for Car running in the background"
                setShowBadge(false)
                enableVibration(false)
            }
            
            val messageChannel = NotificationChannel(
                MESSAGE_CHANNEL_ID,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "New message notifications"
                setShowBadge(true)
                enableVibration(true)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            manager.createNotificationChannel(messageChannel)
        }
    }
    
    private fun createServiceNotification(): Notification {
        val intent = Intent()
        intent.setClassName(this, "com.scerruti.messagesforcar.MainActivity")
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Messages for Car")
            .setContentText("Monitoring messages in background")
            .setSmallIcon(R.drawable.ic_message)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
    
    private fun setupWebView() {
        webView = WebView(this).apply {
            visibility = View.GONE // Hidden WebView
            
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                userAgentString = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 MessagesForCar/1.0"
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            
            // Add JavaScript interface for message interception
            addJavascriptInterface(MessageInterceptor(), "AndroidMessaging")
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page finished loading: $url")
                    
                    if (url?.contains("messages.google.com") == true) {
                        injectMessageInterceptor()
                    }
                }
                
                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "WebView error: ${error?.description}")
                }
            }
            
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d(TAG, "Console: ${consoleMessage?.message()}")
                    return true
                }
            }
        }
    }
    
    private fun injectMessageInterceptor() {
        // JavaScript code to monitor for new messages
        val jsCode = """
            (function() {
                console.log('Messages for Car: Message interceptor injected');
                
                // Function to extract message data
                function extractMessageData(messageElement) {
                    try {
                        // These selectors may need adjustment based on Google Messages structure
                        const senderElement = messageElement.querySelector('[data-sender-name], .sender-name, [aria-label*="From"]');
                        const contentElement = messageElement.querySelector('.message-content, [data-message-text], .text-content');
                        const timeElement = messageElement.querySelector('[data-timestamp], .timestamp, time');
                        
                        const sender = senderElement ? senderElement.textContent.trim() : 'Unknown';
                        const content = contentElement ? contentElement.textContent.trim() : '';
                        const timestamp = timeElement ? timeElement.getAttribute('datetime') || Date.now() : Date.now();
                        
                        return {
                            sender: sender,
                            content: content,
                            timestamp: timestamp,
                            isValid: content.length > 0
                        };
                    } catch (e) {
                        console.error('Error extracting message data:', e);
                        return { isValid: false };
                    }
                }
                
                // Monitor for new messages using MutationObserver
                const observer = new MutationObserver(function(mutations) {
                    mutations.forEach(function(mutation) {
                        mutation.addedNodes.forEach(function(node) {
                            if (node.nodeType === Node.ELEMENT_NODE) {
                                // Look for message containers
                                const messageElements = node.querySelectorAll 
                                    ? node.querySelectorAll('[data-message-id], .message-bubble, .message-container')
                                    : [];
                                
                                messageElements.forEach(function(messageElement) {
                                    const messageData = extractMessageData(messageElement);
                                    if (messageData.isValid) {
                                        // Send to Android
                                        AndroidMessaging.onNewMessage(
                                            messageData.sender,
                                            messageData.content,
                                            messageData.timestamp
                                        );
                                    }
                                });
                            }
                        });
                    });
                });
                
                // Start observing
                observer.observe(document.body, {
                    childList: true,
                    subtree: true
                });
                
                console.log('Messages for Car: Message monitoring started');
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(jsCode) { result ->
            Log.d(TAG, "JavaScript injected: $result")
        }
    }
    
    /**
     * JavaScript interface for receiving intercepted messages
     */
    inner class MessageInterceptor {
        @JavascriptInterface
        fun onNewMessage(sender: String, content: String, timestampStr: String) {
            Log.d(TAG, "New message intercepted - Sender: $sender, Content: $content")
            
            try {
                val timestamp = timestampStr.toLongOrNull() ?: System.currentTimeMillis()
                createMessageNotification(sender, content, timestamp)
            } catch (e: Exception) {
                Log.e(TAG, "Error processing intercepted message", e)
            }
        }
        
        @JavascriptInterface
        fun onConversationRead(conversationId: String) {
            Log.d(TAG, "Conversation marked as read: $conversationId")
            // Handle read status updates
        }
        
        @JavascriptInterface
        fun onReplySent(sender: String, message: String) {
            Log.d(TAG, "Reply sent to $sender: $message")
            // Could show confirmation or update UI
        }
    }
    
    private fun createMessageNotification(sender: String, content: String, timestamp: Long) {
        messageCounter++
        
        // Create person object for the sender
        val senderPerson = Person.Builder()
            .setName(sender)
            .setKey(sender.hashCode().toString())
            .build()
        
        // Create current user (device user)
        val currentUser = Person.Builder()
            .setName("Me")
            .setKey("current_user")
            .build()
        
        // Create messaging style
        val messagingStyle = NotificationCompat.MessagingStyle(currentUser)
            .addMessage(content, timestamp, senderPerson)
            .setConversationTitle(sender)
            .setGroupConversation(false)
        
        // Create reply action
        val replyAction = MessageActionService.createReplyAction(this, sender, messageCounter)
        
        // Create mark as read action
        val markAsReadAction = MessageActionService.createMarkAsReadAction(this, sender, messageCounter)
        
        // Build notification
        val notification = NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setStyle(messagingStyle)
            .addAction(replyAction)
            .addAction(markAsReadAction)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .build()
        
        // Post notification
        notificationManager.notify(messageCounter, notification)
        
        Log.d(TAG, "Message notification created for: $sender")
    }
    
    /**
     * Send a reply message through the WebView
     */
    private fun sendReplyMessage(sender: String, replyText: String) {
        Log.d(TAG, "Sending reply to $sender: $replyText")
        
        val jsCode = """
            (function() {
                try {
                    // Find the conversation with the sender
                    console.log('Searching for conversation with: $sender');
                    
                    // This is a simplified approach - the actual implementation would need
                    // to match Google Messages' current DOM structure
                    const conversationElements = document.querySelectorAll('[data-conversation], .conversation-item, [aria-label*="$sender"]');
                    
                    if (conversationElements.length > 0) {
                        // Click on the conversation to open it
                        conversationElements[0].click();
                        
                        // Wait a moment for the conversation to load
                        setTimeout(function() {
                            // Find the message input field
                            const messageInput = document.querySelector('textarea[aria-label*="message"], input[placeholder*="message"], .message-input');
                            
                            if (messageInput) {
                                // Set the reply text
                                messageInput.value = '$replyText';
                                messageInput.dispatchEvent(new Event('input', { bubbles: true }));
                                
                                // Find and click the send button
                                const sendButton = document.querySelector('[aria-label*="Send"], .send-button, button[type="submit"]');
                                if (sendButton) {
                                    sendButton.click();
                                    console.log('Reply sent successfully');
                                    AndroidMessaging.onReplySent('$sender', '$replyText');
                                } else {
                                    console.error('Send button not found');
                                }
                            } else {
                                console.error('Message input not found');
                            }
                        }, 1000);
                    } else {
                        console.error('Conversation not found for sender: $sender');
                    }
                } catch (e) {
                    console.error('Error sending reply:', e);
                }
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(jsCode) { result ->
            Log.d(TAG, "Reply JavaScript executed: $result")
        }
    }
    
    /**
     * Mark a conversation as read through the WebView
     */
    private fun markConversationAsRead(sender: String) {
        Log.d(TAG, "Marking conversation with $sender as read")
        
        val jsCode = """
            (function() {
                try {
                    console.log('Marking conversation as read for: $sender');
                    
                    // Find the conversation with the sender
                    const conversationElements = document.querySelectorAll('[data-conversation], .conversation-item, [aria-label*="$sender"]');
                    
                    if (conversationElements.length > 0) {
                        // Click on the conversation to mark it as read
                        conversationElements[0].click();
                        
                        // Additional logic could be added here to explicitly mark as read
                        // depending on Google Messages' interface
                        
                        console.log('Conversation marked as read');
                        AndroidMessaging.onConversationRead('$sender');
                    } else {
                        console.error('Conversation not found for sender: $sender');
                    }
                } catch (e) {
                    console.error('Error marking as read:', e);
                }
            })();
        """.trimIndent()
        
        webView.evaluateJavascript(jsCode) { result ->
            Log.d(TAG, "Mark as read JavaScript executed: $result")
        }
    }
}
