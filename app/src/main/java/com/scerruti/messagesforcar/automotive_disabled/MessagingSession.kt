package com.scerruti.messagesforcar.automotive

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class MessagingSession : Session() {
    
    override fun onCreateScreen(intent: Intent): Screen {
        return MessagingMainScreen(carContext)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new intents when the app is already running
        carContext.getCarService(androidx.car.app.ScreenManager::class.java).push(MessagingMainScreen(carContext))
    }
}
