package com.scerruti.messagesforcar.automotive

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*

class ConversationScreen(
    carContext: CarContext,
    private val contactName: String
) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        return ListTemplate.Builder()
            .setSingleList(
                ItemList.Builder()
                    .addItem(
                        Row.Builder()
                            .setTitle("Type your message or use voice")
                            .build()
                    )
                    .build()
            )
            .setTitle("Message to $contactName")
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle("Voice")
                    .setOnClickListener {
                        // Start voice input
                        startVoiceInput()
                    }
                    .build()
            )
            .build()
    }
    
    private fun startVoiceInput() {
        // This would trigger voice recognition
        // For now, just show a toast
        CarToast.makeText(
            carContext,
            "Voice messaging would start here",
            CarToast.LENGTH_SHORT
        ).show()
    }
}
