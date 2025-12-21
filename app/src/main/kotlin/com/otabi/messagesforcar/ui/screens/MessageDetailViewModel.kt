package com.otabi.messagesforcar.ui.messages

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.otabi.messagesforcar.data.entity.MessageEntity
import com.otabi.messagesforcar.data.entity.MessageStatus
import com.otabi.messagesforcar.data.entity.MessageType
import com.otabi.messagesforcar.data.repository.ConversationRepository
import com.otabi.messagesforcar.data.repository.MessageRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.*

/**
 * ViewModel for the message detail screen.
 * Manages message data, sending messages, and UI state.
 */
class MessageDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val messageRepository = MessageRepository.getInstance(application)
    private val conversationRepository = ConversationRepository.getInstance(application)
    
    private val _messages = MutableLiveData<List<MessageEntity>>()
    val messages: LiveData<List<MessageEntity>> = _messages
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _isSending = MutableLiveData<Boolean>()
    val isSending: LiveData<Boolean> = _isSending
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * Load messages for a specific conversation
     */
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                messageRepository.getMessagesForConversation(conversationId)
                    .catch { exception ->
                        _errorMessage.value = "Failed to load messages: ${exception.message}"
                        _isLoading.value = false
                    }
                    .collect { messageList ->
                        _messages.value = messageList.sortedBy { it.timestamp }
                        _isLoading.value = false
                    }
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to load messages: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Send a new message
     */
    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            _isSending.value = true
            _errorMessage.value = null
            
            try {
                val messageId = "msg_${UUID.randomUUID()}"
                val currentTime = System.currentTimeMillis()
                
                // Create new message
                val newMessage = MessageEntity(
                    id = messageId,
                    conversationId = conversationId,
                    content = content,
                    timestamp = currentTime,
                    senderId = "me", // TODO: Get actual user ID
                    senderName = "Me",
                    messageType = MessageType.TEXT,
                    status = MessageStatus.PENDING,
                    mediaUri = null,
                    mediaType = null,
                    isFromMe = true,
                    readTimestamp = null,
                    deliveredTimestamp = null
                )
                
                // Insert the message locally
                messageRepository.insertMessage(newMessage)
                
                // Update conversation's last message
                conversationRepository.updateLastMessage(
                    conversationId = conversationId,
                    messageId = messageId,
                    preview = content,
                    timestamp = currentTime
                )
                
                // TODO: Send message to actual messaging service
                // For now, simulate success after a short delay
                kotlinx.coroutines.delay(1000)
                
                // Update message status to sent
                val sentMessage = newMessage.copy(
                    status = MessageStatus.SENT,
                    deliveredTimestamp = System.currentTimeMillis()
                )
                messageRepository.updateMessage(sentMessage)
                
                _isSending.value = false
                
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to send message: ${exception.message}"
                _isSending.value = false
            }
        }
    }
    
    /**
     * Mark messages as read
     */
    fun markMessagesAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                messageRepository.markConversationAsRead(conversationId, currentTime)
                conversationRepository.updateUnreadCount(conversationId, 0)
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to mark messages as read: ${exception.message}"
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Show error message
     */
    fun showError(message: String) {
        _errorMessage.value = message
    }
    
    /**
     * Delete a message
     */
    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            try {
                val message = messageRepository.getMessageById(messageId)
                if (message != null) {
                    messageRepository.deleteMessage(message)
                }
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to delete message: ${exception.message}"
            }
        }
    }
    
    /**
     * Retry sending a failed message
     */
    fun retryMessage(message: MessageEntity) {
        if (message.status == MessageStatus.FAILED) {
            sendMessage(message.conversationId, message.content)
        }
    }
}
