package com.scerruti.messagesforcar.automotive

import android.content.Intent
import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

class MessagingCarAppService : CarAppService() {
    
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }
    
    override fun onCreateSession(): Session {
        return MessagingSession()
    }
}
