package com.iptvapp.player.domain.usecase

import com.iptvapp.player.domain.model.*
import com.iptvapp.player.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLiveChannelsUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Channel>>> =
        contentRepository.getLiveChannels(categoryId)
}

class GetMoviesUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Movie>>> =
        contentRepository.getMovies(categoryId)
}

class GetSeriesUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(categoryId: String? = null): Flow<Result<List<Series>>> =
        contentRepository.getSeries(categoryId)
}

class GetCategoriesUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    fun getLiveCategories(): Flow<Result<List<Category>>> = contentRepository.getLiveCategories()
    fun getVodCategories(): Flow<Result<List<Category>>> = contentRepository.getVodCategories()
    fun getSeriesCategories(): Flow<Result<List<Category>>> = contentRepository.getSeriesCategories()
}

class GetStreamUrlUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    operator fun invoke(streamId: Int, type: StreamType, extension: String = "m3u8"): StreamUrl =
        contentRepository.buildStreamUrl(streamId, type, extension)
}

class ToggleFavoriteUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    suspend fun toggleChannel(streamId: Int, isFavorite: Boolean) =
        contentRepository.toggleChannelFavorite(streamId, isFavorite)

    suspend fun toggleMovie(streamId: Int, isFavorite: Boolean) =
        contentRepository.toggleMovieFavorite(streamId, isFavorite)

    suspend fun toggleSeries(seriesId: Int, isFavorite: Boolean) =
        contentRepository.toggleSeriesFavorite(seriesId, isFavorite)
}

class GetFavoritesUseCase @Inject constructor(
    private val contentRepository: ContentRepository
) {
    fun getChannels(): Flow<List<Channel>> = contentRepository.getFavoriteChannels()
    fun getMovies(): Flow<List<Movie>> = contentRepository.getFavoriteMovies()
    fun getSeries(): Flow<List<Series>> = contentRepository.getFavoriteSeries()
}
