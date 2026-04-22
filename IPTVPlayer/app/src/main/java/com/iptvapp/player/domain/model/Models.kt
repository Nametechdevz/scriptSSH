package com.iptvapp.player.domain.model

data class UserSession(
    val username: String,
    val serverUrl: String,
    val status: String,
    val expDate: String?,
    val maxConnections: Int,
    val isTrial: Boolean
)

data class Category(
    val id: String,
    val name: String,
    val parentId: Int = 0
)

data class Channel(
    val streamId: Int,
    val name: String,
    val iconUrl: String?,
    val categoryId: String?,
    val epgChannelId: String?,
    val hasArchive: Boolean,
    val isFavorite: Boolean = false
)

data class Movie(
    val streamId: Int,
    val name: String,
    val posterUrl: String?,
    val categoryId: String?,
    val rating: Double,
    val containerExtension: String,
    val isFavorite: Boolean = false
)

data class Series(
    val seriesId: Int,
    val name: String,
    val coverUrl: String?,
    val plot: String?,
    val genre: String?,
    val rating: Double,
    val categoryId: String?,
    val isFavorite: Boolean = false
)

data class StreamUrl(
    val url: String,
    val type: StreamType
)

enum class StreamType { LIVE, VOD, SERIES }

data class ContentSection(
    val title: String,
    val items: List<ContentItem>
)

sealed class ContentItem {
    data class ChannelItem(val channel: Channel) : ContentItem()
    data class MovieItem(val movie: Movie) : ContentItem()
    data class SeriesItem(val series: Series) : ContentItem()
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
