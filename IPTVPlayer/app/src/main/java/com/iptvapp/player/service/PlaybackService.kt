package com.iptvapp.player.service

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    @Inject lateinit var player: ExoPlayer

    private var mediaSession: androidx.media3.session.MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        mediaSession = androidx.media3.session.MediaSession.Builder(this, player).build()
    }

    override fun onGetSession(controllerInfo: androidx.media3.session.MediaSession.ControllerInfo) =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
