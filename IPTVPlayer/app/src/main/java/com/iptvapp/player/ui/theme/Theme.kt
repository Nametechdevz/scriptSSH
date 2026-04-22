package com.iptvapp.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RectangleShape

private val DarkColorScheme = darkColorScheme(
    primary = NetflixRed,
    onPrimary = TextPrimary,
    primaryContainer = NetflixRedDark,
    onPrimaryContainer = TextPrimary,
    secondary = NetflixSubtitle,
    onSecondary = NetflixBlack,
    background = NetflixBlack,
    onBackground = TextPrimary,
    surface = NetflixDarkGray,
    onSurface = TextPrimary,
    surfaceVariant = NetflixMediumGray,
    onSurfaceVariant = TextSecondary,
    outline = NetflixDivider,
    error = FavoriteRed,
    onError = TextPrimary
)

@Composable
fun IPTVPlayerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = IPTVTypography,
        content = content
    )
}
