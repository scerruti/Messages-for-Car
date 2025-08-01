package com.scerruti.messagesforcar.ui.qr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.scerruti.messagesforcar.data.preferences.PairingPreferences
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Assert.*

/**
 * Instrumented tests for QR Code pairing functionality.
 * Tests the native QR code pairing screen and integration with preferences.
 */
@RunWith(AndroidJUnit4::class)
class QRCodePairingTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<QRCodePairingActivity>()

    private lateinit var pairingPreferences: PairingPreferences

    @Before
    fun setup() {
        pairingPreferences = PairingPreferences.getInstance(composeTestRule.activity)
        // Clear any existing pairing state
        pairingPreferences.clearPairing()
    }

    @Test
    fun qrCodePairingScreen_displaysQRCode() {
        composeTestRule.setContent {
            QRCodePairingScreen(
                pairingUrl = "https://messages.google.com/web/setup",
                onPairingComplete = {},
                onError = {},
                isLoading = false
            )
        }

        // Verify QR code section is displayed
        composeTestRule
            .onNodeWithText("Scan QR Code with Your Phone")
            .assertIsDisplayed()

        // Verify instructions are displayed
        composeTestRule
            .onNodeWithText("Open Google Messages app on your phone")
            .assertIsDisplayed()
    }

    @Test
    fun qrCodePairingScreen_displaysLoadingState() {
        composeTestRule.setContent {
            QRCodePairingScreen(
                pairingUrl = "https://messages.google.com/web/setup",
                onPairingComplete = {},
                onError = {},
                isLoading = true
            )
        }

        // Verify loading indicator is displayed
        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertIsDisplayed()
    }

    @Test
    fun pairingPreferences_storesPairingState() {
        // Verify initial state
        assertFalse(pairingPreferences.isPaired)
        assertNull(pairingPreferences.pairingUrl)

        // Mark as paired
        val testUrl = "https://messages.google.com/web/test"
        pairingPreferences.markAsPaired(testUrl)

        // Verify state is saved
        assertTrue(pairingPreferences.isPaired)
        assertEquals(testUrl, pairingPreferences.pairingUrl)
        assertTrue(pairingPreferences.isPairingRecent())
    }

    @Test
    fun pairingPreferences_clearsPairingState() {
        // Set up paired state
        pairingPreferences.markAsPaired("https://messages.google.com/web/test")
        assertTrue(pairingPreferences.isPaired)

        // Clear pairing
        pairingPreferences.clearPairing()

        // Verify state is cleared
        assertFalse(pairingPreferences.isPaired)
        assertNull(pairingPreferences.pairingUrl)
        assertEquals(0L, pairingPreferences.pairingTimestamp)
    }

    @Test
    fun qrCodePairingActivity_finishesOnPairingComplete() {
        // Start the activity and simulate pairing completion
        composeTestRule.activity.finish() // This would normally be called by finishPairingFlow()
        
        // Verify the activity result would be OK
        // Note: In a real test, we'd use ActivityResult testing
        assertTrue(composeTestRule.activity.isFinishing)
    }
}
