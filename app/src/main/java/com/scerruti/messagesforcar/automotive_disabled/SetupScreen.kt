package com.scerruti.messagesforcar.automotive

import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.model.*

class SetupScreen(carContext: CarContext) : Screen(carContext) {
    
    override fun onGetTemplate(): Template {
        return PaneTemplate.Builder(
            Pane.Builder()
                .addRow(
                    Row.Builder()
                        .setTitle("Setup Messages for Car")
                        .addText("To use Messages for Car, you need to pair with Google Messages on your phone.")
                        .build()
                )
                .addRow(
                    Row.Builder()
                        .setTitle("Steps:")
                        .addText("1. Open Google Messages on your phone")
                        .addText("2. Go to Settings > Device pairing")
                        .addText("3. Scan the QR code (only when parked)")
                        .build()
                )
                .addAction(
                    Action.Builder()
                        .setTitle("Open Pairing")
                        .setOnClickListener {
                            openPairingScreen()
                        }
                        .build()
                )
                .build()
        )
            .setTitle("Setup")
            .setHeaderAction(Action.BACK)
            .build()
    }
    
    private fun openPairingScreen() {
        // This would show the QR code for pairing
        CarToast.makeText(
            carContext,
            "QR code pairing only available when parked",
            CarToast.LENGTH_LONG
        ).show()
    }
}
