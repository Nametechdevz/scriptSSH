package com.iptvapp.player.ui.screens.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import com.iptvapp.player.domain.model.StreamType
import com.iptvapp.player.ui.components.PlayerControls
import com.iptvapp.player.ui.theme.NetflixRed

@Composable
fun PlayerScreen(
    streamId: Int,
    streamType: StreamType,
    title: String,
    extension: String = "m3u8",
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(streamId) {
        viewModel.loadStream(streamId, streamType, title, extension)
        viewModel.showControlsTemporarily()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { viewModel.toggleControls() }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    player = viewModel.player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (uiState.isBuffering) {
            CircularProgressIndicator(
                color = NetflixRed,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        PlayerControls(
            isPlaying = uiState.isPlaying,
            isVisible = uiState.isControlsVisible,
            title = uiState.title,
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            isLive = uiState.isLive,
            onPlayPause = viewModel::togglePlayPause,
            onSeekBack = viewModel::seekBack,
            onSeekForward = viewModel::seekForward,
            onFullscreen = {},
            onBack = onBack,
            modifier = Modifier.fillMaxSize()
        )
    }
}
