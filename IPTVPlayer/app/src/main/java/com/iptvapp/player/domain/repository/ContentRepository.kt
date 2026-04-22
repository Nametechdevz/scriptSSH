package com.iptvapp.player.domain.repository

import com.iptvapp.player.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun getLiveChannels(categoryId: String? = null): Flow<Result<List<Channel>>>
    fun getMovies(categoryId: String? = null): Flow<Result<List<Movie>>>
    fun getSeries(categoryId: String? = null): Flow<Result<List<Series>>>
    fun getLiveCategories(): Flow<Result<List<Category>>>
    fun getVodCategories(): Flow<Result<List<Category>>>
    fun getSeriesCategories(): Flow<Result<List<Category>>>
    suspend fun toggleChannelFavorite(streamId: Int, isFavorite: Boolean)
    suspend fun toggleMovieFavorite(streamId: Int, isFavorite: Boolean)
    suspend fun toggleSeriesFavorite(seriesId: Int, isFavorite: Boolean)
    fun getFavoriteChannels(): Flow<List<Channel>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun getFavoriteSeries(): Flow<List<Series>>
    fun buildStreamUrl(streamId: Int, type: StreamType, extension: String = "m3u8"): StreamUrl
}
