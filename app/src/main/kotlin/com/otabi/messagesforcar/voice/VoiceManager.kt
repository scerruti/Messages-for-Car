package com.otabi.messagesforcar.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.otabi.messagesforcar.media.MediaManager

/**
 * Manages voice input using Android SpeechRecognizer.
 * Provides automotive-optimized speech recognition for message replies.
 */
class VoiceManager(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val mediaManager = MediaManager(context)

    init {
        setupSpeechRecognizer()
    }

    private fun setupSpeechRecognizer() {
        // Use applicationContext to avoid leaking the Activity
        val appContext = context.applicationContext

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(object : RecognitionListener {

                override fun onReadyForSpeech(params: Bundle?) {
                    isListening = true
                    Log.d(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Optional: update UI with audio level
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Not used
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                    isListening = false
                    mediaManager.resumeMedia()
                }

                override fun onError(error: Int) {
                    isListening = false
                    mediaManager.resumeMedia()

                    val errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                        SpeechRecognizer.ERROR_NETWORK -> "Network error"
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                        SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                        SpeechRecognizer.ERROR_SERVER -> "Server error"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                        else -> "Unknown error"
                    }

                    Log.e(TAG, "Speech recognition error: $errorMessage")
                    onError(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    isListening = false
                    mediaManager.resumeMedia()

                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val result = matches[0]
                        Log.d(TAG, "Speech result: $result")
                        onResult(result)
                    } else {
                        onError("No speech recognized")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Optional: handle partial results for real-time feedback
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Not used
                }
            })
        }
    }

    fun startListening() {
        if (isListening) return

        // Recreate recognizer if destroyed
        if (speechRecognizer == null) {
            setupSpeechRecognizer()
        }

        mediaManager.pauseMedia()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your message")
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        if (isListening) {
            speechRecognizer?.stopListening()
            isListening = false
            mediaManager.resumeMedia()
        }
    }

    fun destroy() {
        if (isListening) {
            stopListening()
        }
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    fun isListening(): Boolean = isListening

    private companion object {
        const val TAG = "VoiceManager"
    }
}
