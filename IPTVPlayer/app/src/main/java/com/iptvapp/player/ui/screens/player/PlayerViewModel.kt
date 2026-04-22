package com.iptvapp.player.ui.screens.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.iptvapp.player.domain.model.StreamType
import com.iptvapp.player.domain.usecase.GetStreamUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isControlsVisible: Boolean = true,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isBuffering: Boolean = true,
    val error: String? = null,
    val title: String = "",
    val isLive: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getStreamUrlUseCase: GetStreamUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val player: ExoPlayer = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update {
                    it.copy(isBuffering = playbackState == Player.STATE_BUFFERING)
                }
            }
        })
    }

    private var controlsHideJob: kotlinx.coroutines.Job? = null

    init {
        startPositionUpdates()
    }

    fun loadStream(streamId: Int, streamType: StreamType, title: String, extension: String = "m3u8") {
        val isLive = streamType == StreamType.LIVE
        _uiState.update { it.copy(title = title, isLive = isLive) }

        val streamUrl = getStreamUrlUseCase(streamId, streamType, extension)
        val mediaItem = MediaItem.fromUri(streamUrl.url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
        showControlsTemporarily()
    }

    fun seekBack() {
        player.seekTo((player.currentPosition - 10_000L).coerceAtLeast(0L))
        showControlsTemporarily()
    }

    fun seekForward() {
        player.seekTo((player.currentPosition + 10_000L).coerceAtMost(player.duration))
        showControlsTemporarily()
    }

    fun toggleControls() {
        val current = _uiState.value.isControlsVisible
        _uiState.update { it.copy(isControlsVisible = !current) }
        if (!current) showControlsTemporarily()
    }

    fun showControlsTemporarily() {
        _uiState.update { it.copy(isControlsVisible = true) }
        controlsHideJob?.cancel()
        controlsHideJob = viewModelScope.launch {
            delay(4000)
            _uiState.update { it.copy(isControlsVisible = false) }
        }
    }

    private fun startPositionUpdates() {
        viewModelScope.launch {
            while (true) {
                _uiState.update {
                    it.copy(
                        currentPosition = player.currentPosition,
                        duration = player.duration.coerceAtLeast(0L)
                    )
                }
                delay(500)
            }
        }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
