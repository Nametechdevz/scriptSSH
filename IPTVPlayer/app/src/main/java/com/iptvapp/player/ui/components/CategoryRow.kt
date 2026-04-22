package com.iptvapp.player.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvapp.player.domain.model.Channel
import com.iptvapp.player.domain.model.Movie
import com.iptvapp.player.domain.model.Series
import com.iptvapp.player.ui.theme.TextPrimary

@Composable
fun ChannelCategoryRow(
    title: String,
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit,
    modifier: Modifier = Modifier
) {
    if (channels.isEmpty()) return

    Column(modifier = modifier) {
        SectionHeader(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(channels, key = { it.streamId }) { channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    onFavoriteClick = { onFavoriteClick(channel) }
                )
            }
        }
    }
}

@Composable
fun MovieCategoryRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onFavoriteClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    Column(modifier = modifier) {
        SectionHeader(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movies, key = { it.streamId }) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onMovieClick(movie) },
                    onFavoriteClick = { onFavoriteClick(movie) }
                )
            }
        }
    }
}

@Composable
fun SeriesCategoryRow(
    title: String,
    seriesList: List<Series>,
    onSeriesClick: (Series) -> Unit,
    onFavoriteClick: (Series) -> Unit,
    modifier: Modifier = Modifier
) {
    if (seriesList.isEmpty()) return

    Column(modifier = modifier) {
        SectionHeader(title = title)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(seriesList, key = { it.seriesId }) { series ->
                SeriesCard(
                    series = series,
                    onClick = { onSeriesClick(series) },
                    onFavoriteClick = { onFavoriteClick(series) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        color = TextPrimary,
        modifier = modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}
