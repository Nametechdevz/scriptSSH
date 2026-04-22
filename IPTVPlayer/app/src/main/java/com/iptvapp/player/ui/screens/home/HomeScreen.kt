package com.iptvapp.player.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iptvapp.player.domain.model.Channel
import com.iptvapp.player.domain.model.Movie
import com.iptvapp.player.domain.model.Series
import com.iptvapp.player.domain.model.StreamType
import com.iptvapp.player.ui.components.*
import com.iptvapp.player.ui.theme.*

@Composable
fun HomeScreen(
    onChannelClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSeriesClick: (Series) -> Unit,
    onAdminClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NetflixBlack)
    ) {
        TopBar(title = "IPTV", onAdminClick = onAdminClick)

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = NetflixBlack,
            contentColor = NetflixRed,
            edgePadding = 16.dp,
            divider = { HorizontalDivider(color = NetflixDivider, thickness = 1.dp) }
        ) {
            HomeTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = tab.displayName(),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (uiState.selectedTab == tab) NetflixRed else TextSecondary
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.selectedTab) {
                HomeTab.LIVE -> LiveTvContent(
                    channels = uiState.channels,
                    isLoading = uiState.isLoadingChannels,
                    onChannelClick = onChannelClick,
                    onFavoriteClick = viewModel::toggleChannelFavorite
                )
                HomeTab.MOVIES -> MoviesContent(
                    movies = uiState.movies,
                    isLoading = uiState.isLoadingMovies,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = viewModel::toggleMovieFavorite
                )
                HomeTab.SERIES -> SeriesContent(
                    seriesList = uiState.seriesList,
                    isLoading = uiState.isLoadingSeries,
                    onSeriesClick = onSeriesClick,
                    onFavoriteClick = viewModel::toggleSeriesFavorite
                )
                HomeTab.FAVORITES -> FavoritesContent(
                    favoriteChannels = uiState.favoriteChannels,
                    favoriteMovies = uiState.favoriteMovies,
                    onChannelClick = onChannelClick,
                    onMovieClick = onMovieClick
                )
            }

            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = NetflixSurface
                ) {
                    Text(uiState.error ?: "", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun LiveTvContent(
    channels: List<Channel>,
    isLoading: Boolean,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit
) {
    if (isLoading && channels.isEmpty()) {
        LoadingIndicator()
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ChannelCategoryRow(
                title = "All Channels",
                channels = channels,
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
        // Group by first letter for variety
        val grouped = channels.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
            .entries.take(5)
        items(grouped.size) { index ->
            val (letter, group) = grouped.elementAt(index)
            ChannelCategoryRow(
                title = "— $letter —",
                channels = group,
                onChannelClick = onChannelClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun MoviesContent(
    movies: List<Movie>,
    isLoading: Boolean,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit
) {
    if (isLoading && movies.isEmpty()) {
        LoadingIndicator()
        return
    }

    val topRated = movies.filter { it.rating >= 4.0 }.sortedByDescending { it.rating }
    val recent = movies.take(20)

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        if (topRated.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title = "Top Rated",
                    movies = topRated.take(20),
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
        item {
            MovieCategoryRow(
                title = "All Movies",
                movies = recent,
                onMovieClick = onMovieClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun SeriesContent(
    seriesList: List<Series>,
    isLoading: Boolean,
    onSeriesClick: (Series) -> Unit,
    onFavoriteClick: (Series) -> Unit
) {
    if (isLoading && seriesList.isEmpty()) {
        LoadingIndicator()
        return
    }

    val topRated = seriesList.filter { it.rating >= 4.0 }.sortedByDescending { it.rating }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        if (topRated.isNotEmpty()) {
            item {
                SeriesCategoryRow(
                    title = "Top Series",
                    seriesList = topRated.take(20),
                    onSeriesClick = onSeriesClick,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
        item {
            SeriesCategoryRow(
                title = "All Series",
                seriesList = seriesList.take(30),
                onSeriesClick = onSeriesClick,
                onFavoriteClick = onFavoriteClick
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    favoriteChannels: List<Channel>,
    favoriteMovies: List<Movie>,
    onChannelClick: (Channel) -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    if (favoriteChannels.isEmpty() && favoriteMovies.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = TextDisabled,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No favorites yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextSecondary
                )
            }
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
        if (favoriteChannels.isNotEmpty()) {
            item {
                ChannelCategoryRow(
                    title = "Favorite Channels",
                    channels = favoriteChannels,
                    onChannelClick = onChannelClick,
                    onFavoriteClick = {}
                )
            }
        }
        if (favoriteMovies.isNotEmpty()) {
            item {
                MovieCategoryRow(
                    title = "Favorite Movies",
                    movies = favoriteMovies,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = {}
                )
            }
        }
    }
}

private fun HomeTab.displayName() = when (this) {
    HomeTab.LIVE -> "Live TV"
    HomeTab.MOVIES -> "Movies"
    HomeTab.SERIES -> "Series"
    HomeTab.FAVORITES -> "Favorites"
}
