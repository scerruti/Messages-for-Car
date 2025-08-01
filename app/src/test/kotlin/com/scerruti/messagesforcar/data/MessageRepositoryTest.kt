package com.scerruti.messagesforcar.data

import com.scerruti.messagesforcar.data.entity.MessageEntity
import com.scerruti.messagesforcar.data.entity.MessageStatus
import com.scerruti.messagesforcar.data.entity.MessageType
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Message data models.
 * Tests basic functionality and automotive-specific features.
 */
class MessageDataModelTest {
    
    @Test
    fun messageEntity_propertiesCorrectlySet() {
        val timestamp = System.currentTimeMillis()
        
        val message = MessageEntity(
            id = "msg_123",
            conversationId = "conv_123",
            content = "Test message",
            timestamp = timestamp,
            senderId = "sender_123",
            senderName = "Test Sender",
            messageType = MessageType.TEXT,
            status = MessageStatus.SENT,
            isFromMe = true
        )
        
        assertEquals("msg_123", message.id)
        assertEquals("conv_123", message.conversationId)
        assertEquals("Test message", message.content)
        assertEquals(timestamp, message.timestamp)
        assertEquals("sender_123", message.senderId)
        assertEquals("Test Sender", message.senderName)
        assertEquals(MessageType.TEXT, message.messageType)
        assertEquals(MessageStatus.SENT, message.status)
        assertTrue(message.isFromMe)
        assertNull(message.mediaUri)
        assertNull(message.readTimestamp)
    }
    
    @Test
    fun messageType_allTypesAvailable() {
        // Verify all expected message types are available
        val types = MessageType.values()
        
        assertTrue(types.contains(MessageType.TEXT))
        assertTrue(types.contains(MessageType.IMAGE))
        assertTrue(types.contains(MessageType.VIDEO))
        assertTrue(types.contains(MessageType.AUDIO))
        assertTrue(types.contains(MessageType.VOICE_MESSAGE))
        assertTrue(types.contains(MessageType.FILE))
        assertTrue(types.contains(MessageType.LOCATION))
        assertTrue(types.contains(MessageType.CONTACT))
    }
    
    @Test
    fun messageStatus_allStatusesAvailable() {
        // Verify all expected message statuses are available
        val statuses = MessageStatus.values()
        
        assertTrue(statuses.contains(MessageStatus.PENDING))
        assertTrue(statuses.contains(MessageStatus.SENT))
        assertTrue(statuses.contains(MessageStatus.DELIVERED))
        assertTrue(statuses.contains(MessageStatus.READ))
        assertTrue(statuses.contains(MessageStatus.FAILED))
    }
    
    @Test
    fun messageEntity_voiceMessageType() {
        val message = MessageEntity(
            id = "voice_msg_123",
            conversationId = "conv_123",
            content = "Voice message",
            timestamp = System.currentTimeMillis(),
            senderId = null,
            senderName = null,
            messageType = MessageType.VOICE_MESSAGE,
            status = MessageStatus.DELIVERED,
            mediaUri = "file://voice_recording.mp3",
            mediaType = "audio/mp3",
            isFromMe = false
        )
        
        assertEquals(MessageType.VOICE_MESSAGE, message.messageType)
        assertEquals("file://voice_recording.mp3", message.mediaUri)
        assertEquals("audio/mp3", message.mediaType)
        assertFalse(message.isFromMe)
    }
}
