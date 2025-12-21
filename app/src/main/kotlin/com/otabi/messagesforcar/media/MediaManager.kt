package com.otabi.messagesforcar.media

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import androidx.media.session.MediaButtonReceiver

/**
 * Manages media playback control for driving safety.
 * Pauses/resumes active media sessions during voice input.
 */
class MediaManager(private val context: Context) {

    private var mediaController: MediaController? = null
    private var wasPlaying = false

    init {
        // Try to get active media session
        val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        try {
            val sessions = mediaSessionManager.getActiveSessions(null)
            mediaController = sessions.firstOrNull()
        } catch (e: SecurityException) {
            // Permission denied, can't control media
        }
    }

    /**
     * Pause active media playback.
     */
    fun pauseMedia() {
        wasPlaying = false

        mediaController?.let { controller ->
            if (controller.playbackState?.state == PlaybackState.STATE_PLAYING) {
                wasPlaying = true
                controller.transportControls.pause()
            }
        }
    }

    /**
     * Resume media playback if it was paused by us.
     */
    fun resumeMedia() {
        if (!wasPlaying) return

        mediaController?.transportControls?.play()

        wasPlaying = false
    }
}