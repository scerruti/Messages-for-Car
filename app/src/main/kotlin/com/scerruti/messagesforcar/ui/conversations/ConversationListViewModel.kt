package com.scerruti.messagesforcar.ui.conversations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.scerruti.messagesforcar.data.entity.ConversationEntity
import com.scerruti.messagesforcar.data.repository.ConversationRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel for the conversation list screen.
 * Manages conversation data and UI state.
 */
class ConversationListViewModel(application: Application) : AndroidViewModel(application) {
    
    private val conversationRepository = ConversationRepository.getInstance(application)
    
    private val _conversations = MutableLiveData<List<ConversationEntity>>()
    val conversations: LiveData<List<ConversationEntity>> = _conversations
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    init {
        loadConversations()
    }
    
    /**
     * Load conversations from the repository
     */
    private fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                conversationRepository.getAllActiveConversations()
                    .catch { exception ->
                        _errorMessage.value = "Failed to load conversations: ${exception.message}"
                        _isLoading.value = false
                    }
                    .collect { conversationList ->
                        _conversations.value = conversationList
                        _isLoading.value = false
                    }
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to load conversations: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh conversations
     */
    fun refreshConversations() {
        loadConversations()
    }
    
    /**
     * Archive a conversation
     */
    fun archiveConversation(conversationId: String) {
        viewModelScope.launch {
            try {
                conversationRepository.setConversationArchived(conversationId, true)
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to archive conversation: ${exception.message}"
            }
        }
    }
    
    /**
     * Pin or unpin a conversation
     */
    @Suppress("UNUSED_PARAMETER")
    fun toggleConversationPin(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            try {
                // Note: Pin functionality not implemented in repository yet
                _errorMessage.value = "Pin feature not yet implemented"
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to update conversation: ${exception.message}"
            }
        }
    }
    
    /**
     * Mark conversation as read
     */
    fun markConversationAsRead(conversationId: String) {
        viewModelScope.launch {
            try {
                conversationRepository.updateUnreadCount(conversationId, 0)
            } catch (exception: Exception) {
                _errorMessage.value = "Failed to mark conversation as read: ${exception.message}"
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
     * Search conversations by display name or phone number
     */
    fun searchConversations(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                if (query.isBlank()) {
                    loadConversations()
                } else {
                    val searchResults = conversationRepository.searchConversations(query)
                    _conversations.value = searchResults
                    _isLoading.value = false
                }
            } catch (exception: Exception) {
                _errorMessage.value = "Search failed: ${exception.message}"
                _isLoading.value = false
            }
        }
    }
}
