package com.scerruti.messagesforcar.data.repository

import android.content.Context
import com.scerruti.messagesforcar.data.dao.MessageDao
import com.scerruti.messagesforcar.data.database.MessagesDatabase
import com.scerruti.messagesforcar.data.entity.MessageEntity
import com.scerruti.messagesforcar.data.entity.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing message data.
 * Provides a clean API for message operations following MVVM architecture.
 */
class MessageRepository(
    private val messageDao: MessageDao
) {
    
    companion object {
        @Volatile
        private var INSTANCE: MessageRepository? = null
        
        fun getInstance(context: Context): MessageRepository {
            return INSTANCE ?: synchronized(this) {
                val database = MessagesDatabase.getDatabase(context)
                INSTANCE ?: MessageRepository(database.messageDao()).also { INSTANCE = it }
            }
        }
    }
    
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }
    
    suspend fun getMessagesForConversationPaged(
        conversationId: String,
        limit: Int,
        offset: Int
    ): List<MessageEntity> {
        return messageDao.getMessagesForConversationPaged(conversationId, limit, offset)
    }
    
    suspend fun getMessageById(messageId: String): MessageEntity? {
        return messageDao.getMessageById(messageId)
    }
    
    suspend fun getLastMessageForConversation(conversationId: String): MessageEntity? {
        return messageDao.getLastMessageForConversation(conversationId)
    }
    
    suspend fun getUnreadMessageCount(conversationId: String): Int {
        return messageDao.getUnreadMessageCount(conversationId)
    }
    
    suspend fun insertMessage(message: MessageEntity) {
        messageDao.insertMessage(message)
    }
    
    suspend fun insertMessages(messages: List<MessageEntity>) {
        messageDao.insertMessages(messages)
    }
    
    suspend fun updateMessage(message: MessageEntity) {
        messageDao.updateMessage(message)
    }
    
    suspend fun updateMessageStatus(messageId: String, status: MessageStatus) {
        messageDao.updateMessageStatus(messageId, status)
    }
    
    suspend fun markConversationAsRead(conversationId: String, timestamp: Long = System.currentTimeMillis()) {
        messageDao.markConversationAsRead(conversationId, timestamp)
    }
    
    suspend fun deleteMessage(message: MessageEntity) {
        messageDao.deleteMessage(message)
    }
    
    suspend fun deleteMessagesForConversation(conversationId: String) {
        messageDao.deleteMessagesForConversation(conversationId)
    }
    
    suspend fun deleteOldMessages(timestamp: Long) {
        messageDao.deleteOldMessages(timestamp)
    }
    
    // Automotive-specific methods
    suspend fun getRecentVoiceMessages(conversationId: String, limit: Int = 10): List<MessageEntity> {
        return messageDao.getRecentVoiceMessages(conversationId, limit)
    }
    
    suspend fun getFailedMessages(): List<MessageEntity> {
        return messageDao.getFailedMessages()
    }
    
    /**
     * Get all messages synchronously (for WorkManager use)
     */
    suspend fun getAllMessagesSync(): List<MessageEntity> {
        return messageDao.getAllMessages()
    }
    
    /**
     * Creates a new text message entity.
     * Utility method for common message creation.
     */
    fun createTextMessage(
        id: String = generateMessageId(),
        conversationId: String,
        content: String,
        isFromMe: Boolean,
        senderId: String? = null,
        senderName: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): MessageEntity {
        return MessageEntity(
            id = id,
            conversationId = conversationId,
            content = content,
            timestamp = timestamp,
            senderId = senderId,
            senderName = senderName,
            messageType = com.scerruti.messagesforcar.data.entity.MessageType.TEXT,
            status = if (isFromMe) MessageStatus.PENDING else MessageStatus.DELIVERED,
            isFromMe = isFromMe
        )
    }
    
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
