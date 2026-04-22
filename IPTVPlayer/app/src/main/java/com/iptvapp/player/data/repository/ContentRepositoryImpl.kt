package com.iptvapp.player.data.repository

import com.iptvapp.player.data.local.dao.ChannelDao
import com.iptvapp.player.data.local.dao.MovieDao
import com.iptvapp.player.data.local.dao.SeriesDao
import com.iptvapp.player.data.local.entity.ChannelEntity
import com.iptvapp.player.data.local.entity.MovieEntity
import com.iptvapp.player.data.local.entity.SeriesEntity
import com.iptvapp.player.data.preferences.EncryptedPreferencesManager
import com.iptvapp.player.data.remote.XtreamCodesApi
import com.iptvapp.player.domain.model.*
import com.iptvapp.player.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XtreamCodesApi,
    private val prefs: EncryptedPreferencesManager,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao
) : ContentRepository {

    private val username get() = prefs.getUsername() ?: ""
    private val password get() = prefs.getPassword() ?: ""

    override fun getLiveChannels(categoryId: String?): Flow<Result<List<Channel>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getLiveStreams(username, password, categoryId = categoryId)
            val entities = dtos.map {
                ChannelEntity(
                    streamId = it.streamId,
                    name = it.name,
                    streamIcon = it.streamIcon,
                    categoryId = it.categoryId,
                    epgChannelId = it.epgChannelId,
                    tvArchive = it.tvArchive,
                    tvArchiveDuration = it.tvArchiveDuration
                )
            }
            channelDao.insertChannels(entities)
            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load channels: ${e.message}", e))
        }
    }

    override fun getMovies(categoryId: String?): Flow<Result<List<Movie>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getVodStreams(username, password, categoryId = categoryId)
            val entities = dtos.map {
                MovieEntity(
                    streamId = it.streamId,
                    name = it.name,
                    streamIcon = it.streamIcon,
                    categoryId = it.categoryId,
                    rating = it.rating5based,
                    containerExtension = it.containerExtension
                )
            }
            movieDao.insertMovies(entities)
            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load movies: ${e.message}", e))
        }
    }

    override fun getSeries(categoryId: String?): Flow<Result<List<Series>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getSeries(username, password, categoryId = categoryId)
            val entities = dtos.map {
                SeriesEntity(
                    seriesId = it.seriesId,
                    name = it.name,
                    cover = it.cover,
                    plot = it.plot,
                    genre = it.genre,
                    rating = it.rating5based,
                    categoryId = it.categoryId
                )
            }
            seriesDao.insertSeries(entities)
            emit(Result.Success(entities.map { it.toDomain() }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load series: ${e.message}", e))
        }
    }

    override fun getLiveCategories(): Flow<Result<List<Category>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getLiveCategories(username, password)
            emit(Result.Success(dtos.map { Category(it.categoryId, it.categoryName, it.parentId) }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load categories: ${e.message}", e))
        }
    }

    override fun getVodCategories(): Flow<Result<List<Category>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getVodCategories(username, password)
            emit(Result.Success(dtos.map { Category(it.categoryId, it.categoryName, it.parentId) }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load categories: ${e.message}", e))
        }
    }

    override fun getSeriesCategories(): Flow<Result<List<Category>>> = flow {
        emit(Result.Loading)
        try {
            val dtos = api.getSeriesCategories(username, password)
            emit(Result.Success(dtos.map { Category(it.categoryId, it.categoryName, it.parentId) }))
        } catch (e: Exception) {
            emit(Result.Error("Failed to load categories: ${e.message}", e))
        }
    }

    override suspend fun toggleChannelFavorite(streamId: Int, isFavorite: Boolean) =
        channelDao.setFavorite(streamId, isFavorite)

    override suspend fun toggleMovieFavorite(streamId: Int, isFavorite: Boolean) =
        movieDao.setFavorite(streamId, isFavorite)

    override suspend fun toggleSeriesFavorite(seriesId: Int, isFavorite: Boolean) =
        seriesDao.setFavorite(seriesId, isFavorite)

    override fun getFavoriteChannels(): Flow<List<Channel>> =
        channelDao.getFavoriteChannels().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteMovies(): Flow<List<Movie>> =
        movieDao.getFavoriteMovies().map { list -> list.map { it.toDomain() } }

    override fun getFavoriteSeries(): Flow<List<Series>> =
        seriesDao.getFavoriteSeries().map { list -> list.map { it.toDomain() } }

    override fun buildStreamUrl(streamId: Int, type: StreamType, extension: String): StreamUrl {
        val base = prefs.getServerUrl() ?: ""
        val user = username
        val pass = password
        val url = when (type) {
            StreamType.LIVE -> "${base}live/$user/$pass/$streamId.$extension"
            StreamType.VOD -> "${base}movie/$user/$pass/$streamId.$extension"
            StreamType.SERIES -> "${base}series/$user/$pass/$streamId.$extension"
        }
        return StreamUrl(url, type)
    }

    private fun ChannelEntity.toDomain() = Channel(
        streamId = streamId,
        name = name,
        iconUrl = streamIcon,
        categoryId = categoryId,
        epgChannelId = epgChannelId,
        hasArchive = tvArchive == 1,
        isFavorite = isFavorite
    )

    private fun MovieEntity.toDomain() = Movie(
        streamId = streamId,
        name = name,
        posterUrl = streamIcon,
        categoryId = categoryId,
        rating = rating,
        containerExtension = containerExtension,
        isFavorite = isFavorite
    )

    private fun SeriesEntity.toDomain() = Series(
        seriesId = seriesId,
        name = name,
        coverUrl = cover,
        plot = plot,
        genre = genre,
        rating = rating,
        categoryId = categoryId,
        isFavorite = isFavorite
    )
}
