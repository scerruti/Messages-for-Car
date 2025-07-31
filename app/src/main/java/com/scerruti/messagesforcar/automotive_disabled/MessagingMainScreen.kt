package com.scerruti.messagesforcar.automotive

import android.content.Intent
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*
import androidx.car.app.model.Action

class MessagingMainScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(buildMessageList())
            .setTitle("Messages for Car")
            .setHeaderAction(Action.APP_ICON)
            .addAction(
                Action.Builder()
                    .setTitle("Voice Message")
                    .setOnClickListener {
                        startVoiceMessaging()
                    }
                    .build()
            )
            .build()
    }
    
    private fun buildMessageList(): ItemList {
        val listBuilder = ItemList.Builder()
        
        // Sample conversation items
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Mom")
                .addText("How are you doing?")
                .addText("2 min ago")
                .setOnClickListener {
                    screenManager.push(ConversationScreen(carContext, "Mom"))
                }
                .build()
        )
        
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Work Group")
                .addText("Meeting at 3 PM")
                .addText("5 min ago")
                .setOnClickListener {
                    screenManager.push(ConversationScreen(carContext, "Work Group"))
                }
                .build()
        )
        
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Setup Messages")
                .addText("Tap to pair with your phone")
                .setOnClickListener {
                    openSetupScreen()
                }
                .build()
        )
        
        return listBuilder.build()
    }
    
    private fun startVoiceMessaging() {
        val intent = Intent(carContext, com.scerruti.messagesforcar.VoiceActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        carContext.startActivity(intent)
    }
    
    private fun openSetupScreen() {
        screenManager.push(SetupScreen(carContext))
    }
}
