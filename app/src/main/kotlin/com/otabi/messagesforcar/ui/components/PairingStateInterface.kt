package com.otabi.messagesforcar.ui.components

import android.webkit.JavascriptInterface
import com.otabi.messagesforcar.pairing.PairingState

/**
 * JavaScript interface for detecting pairing state changes.
 */
class PairingStateInterface(private val onPairingStateChanged: ((PairingState) -> Unit)?) {
    @JavascriptInterface
    fun onPairingStateChanged(state: String) {
        val pairingState = when (state) {
            "QR_REQUIRED" -> PairingState.QR_REQUIRED
            "IMAGE_MATCH_REQUIRED" -> PairingState.IMAGE_MATCH_REQUIRED
            "PAIRED" -> PairingState.PAIRED
            else -> PairingState.UNKNOWN
        }
        onPairingStateChanged?.invoke(pairingState)
    }
}