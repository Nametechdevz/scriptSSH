package com.iptvapp.player.ui.screens.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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
    val isLive: Boolean = false,
    val streamUrl: String = ""
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getStreamUrlUseCase: GetStreamUrlUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    // OkHttp client shared for media downloads (handles redirects, timeouts, self-signed certs)
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "IPTVPlayer/1.0 (Android)")
                    .build()
            )
        }
        .build()

    private val httpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

    // DefaultRenderersFactory with PREFER_DECODER_EXTENSIONS tries software decoders
    // for AC3/EAC3/DTS audio that many IPTV streams use — fixes the no-audio bug
    private val renderersFactory = DefaultRenderersFactory(context).apply {
        setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)
    }

    val player: ExoPlayer = ExoPlayer.Builder(context, renderersFactory).build().apply {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _uiState.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        error = if (playbackState == Player.STATE_IDLE) it.error else null
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val msg = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                        "No se pudo conectar al servidor. Verifica la URL."
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                        "Tiempo de conexión agotado."
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS ->
                        "Error del servidor (HTTP ${error.message})."
                    PlaybackException.ERROR_CODE_PARSING_CONTAINER_UNSUPPORTED ->
                        "Formato de stream no compatible."
                    else -> "Error de reproducción: ${error.message}"
                }
                _uiState.update { it.copy(error = msg, isBuffering = false) }
            }
        })
    }

    private var controlsHideJob: kotlinx.coroutines.Job? = null

    init {
        startPositionUpdates()
    }

    fun loadStream(streamId: Int, streamType: StreamType, title: String, extension: String = "m3u8") {
        val isLive = streamType == StreamType.LIVE
        _uiState.update { it.copy(title = title, isLive = isLive, error = null, isBuffering = true) }

        try {
            val streamUrl = getStreamUrlUseCase(streamId, streamType, extension)

            if (streamUrl.url.isBlank() || !streamUrl.url.startsWith("http")) {
                _uiState.update {
                    it.copy(
                        error = "URL inválida: '${streamUrl.url}'. Verifica la configuración del servidor.",
                        isBuffering = false
                    )
                }
                return
            }

            _uiState.update { it.copy(streamUrl = streamUrl.url) }

            val mediaItem = MediaItem.fromUri(streamUrl.url)

            // Use HLS media source for .m3u8, progressive for everything else
            val mediaSource = if (extension == "m3u8" || isLive) {
                HlsMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(httpDataSourceFactory).createMediaSource(mediaItem)
            }

            player.setMediaSource(mediaSource)
            player.prepare()
            player.playWhenReady = true

            showControlsTemporarily()
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    error = "Error iniciando stream: ${e.message}",
                    isBuffering = false
                )
            }
        }
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
        player.seekTo((player.currentPosition + 10_000L).coerceAtMost(player.duration.coerceAtLeast(0L)))
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

    fun retryStream() {
        val state = _uiState.value
        if (state.streamUrl.isNotBlank()) {
            _uiState.update { it.copy(error = null, isBuffering = true) }
            player.prepare()
            player.playWhenReady = true
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
