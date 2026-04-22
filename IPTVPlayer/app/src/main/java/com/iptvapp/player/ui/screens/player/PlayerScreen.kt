package com.iptvapp.player.ui.screens.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.iptvapp.player.domain.model.StreamType
import com.iptvapp.player.ui.components.PlayerControls
import com.iptvapp.player.ui.theme.NetflixRed
import com.iptvapp.player.ui.theme.TextPrimary
import com.iptvapp.player.ui.theme.TextSecondary

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

    LaunchedEffect(streamId, streamType) {
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
        // Video surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    useController = false
                    player = viewModel.player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Buffering spinner — only show when actually buffering and no error
        if (uiState.isBuffering && uiState.error == null) {
            CircularProgressIndicator(
                color = NetflixRed,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error overlay
        if (uiState.error != null) {
            ErrorOverlay(
                error = uiState.error!!,
                streamUrl = uiState.streamUrl,
                onRetry = { viewModel.retryStream() },
                onBack = onBack,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Custom controls overlay
        if (uiState.error == null) {
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
        } else {
            // Back button even on error
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart)
            ) {
                TextButton(onClick = onBack) {
                    Text("← Atrás", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ErrorOverlay(
    error: String,
    streamUrl: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = NetflixRed,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Error de reproducción",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (streamUrl.isNotBlank()) {
            Text(
                text = "URL: $streamUrl",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text("Volver")
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = NetflixRed)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reintentar", color = Color.White)
            }
        }
    }
}
