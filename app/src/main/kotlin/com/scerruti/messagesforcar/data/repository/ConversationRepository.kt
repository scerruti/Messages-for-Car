package com.scerruti.messagesforcar.data.repository

import com.scerruti.messagesforcar.data.dao.ConversationDao
import com.scerruti.messagesforcar.data.entity.ConversationEntity
import com.scerruti.messagesforcar.data.entity.ConversationType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing conversation data.
 * Provides a clean API for conversation operations following MVVM architecture.
 */
class ConversationRepository(
    private val conversationDao: ConversationDao
) {
    
    fun getAllActiveConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getAllActiveConversations()
    }
    
    fun getArchivedConversations(): Flow<List<ConversationEntity>> {
        return conversationDao.getArchivedConversations()
    }
    
    suspend fun getConversationById(conversationId: String): ConversationEntity? {
        return conversationDao.getConversationById(conversationId)
    }
    
    fun getConversationByIdFlow(conversationId: String): Flow<ConversationEntity?> {
        return conversationDao.getConversationByIdFlow(conversationId)
    }
    
    fun getConversationsWithUnreadMessages(): Flow<List<ConversationEntity>> {
        return conversationDao.getConversationsWithUnreadMessages()
    }
    
    suspend fun getTotalUnreadConversationCount(): Int {
        return conversationDao.getTotalUnreadConversationCount()
    }
    
    suspend fun getTotalUnreadMessageCount(): Int {
        return conversationDao.getTotalUnreadMessageCount()
    }
    
    suspend fun insertConversation(conversation: ConversationEntity) {
        conversationDao.insertConversation(conversation)
    }
    
    suspend fun insertConversations(conversations: List<ConversationEntity>) {
        conversationDao.insertConversations(conversations)
    }
    
    suspend fun updateConversation(conversation: ConversationEntity) {
        conversationDao.updateConversation(conversation)
    }
    
    suspend fun updateUnreadCount(conversationId: String, count: Int) {
        conversationDao.updateUnreadCount(conversationId, count)
    }
    
    suspend fun updateLastMessage(
        conversationId: String,
        messageId: String,
        preview: String,
        timestamp: Long
    ) {
        conversationDao.updateLastMessage(
            conversationId,
            messageId,
            preview,
            timestamp,
            System.currentTimeMillis()
        )
    }
    
    suspend fun setConversationArchived(conversationId: String, archived: Boolean) {
        conversationDao.setConversationArchived(conversationId, archived)
    }
    
    suspend fun setConversationMuted(conversationId: String, muted: Boolean) {
        conversationDao.setConversationMuted(conversationId, muted)
    }
    
    suspend fun deleteConversation(conversation: ConversationEntity) {
        conversationDao.deleteConversation(conversation)
    }
    
    suspend fun deleteConversationById(conversationId: String) {
        conversationDao.deleteConversationById(conversationId)
    }
    
    // Automotive-specific methods
    suspend fun getRecentActiveConversations(limit: Int = 5): List<ConversationEntity> {
        return conversationDao.getRecentActiveConversations(limit)
    }
    
    suspend fun searchConversations(query: String): List<ConversationEntity> {
        return conversationDao.searchConversations(query)
    }
    
    /**
     * Creates a new conversation entity.
     * Utility method for common conversation creation.
     */
    fun createConversation(
        id: String = generateConversationId(),
        title: String,
        participants: List<String>,
        participantNames: List<String>,
        isGroup: Boolean = participants.size > 1,
        conversationType: ConversationType = ConversationType.SMS,
        timestamp: Long = System.currentTimeMillis()
    ): ConversationEntity {
        return ConversationEntity(
            id = id,
            title = title,
            participants = participants.joinToString(","),
            participantNames = participantNames.joinToString(","),
            lastMessageId = null,
            lastMessagePreview = null,
            lastMessageTimestamp = timestamp,
            unreadCount = 0,
            isGroup = isGroup,
            conversationType = conversationType,
            createdTimestamp = timestamp,
            updatedTimestamp = timestamp
        )
    }
    
    private fun generateConversationId(): String {
        return "conv_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
