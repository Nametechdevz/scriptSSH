package com.iptvapp.player.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LiveStreamDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("epg_channel_id") val epgChannelId: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("tv_archive") val tvArchive: Int,
    @SerializedName("direct_source") val directSource: String?,
    @SerializedName("tv_archive_duration") val tvArchiveDuration: Int
)

data class MovieDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("stream_type") val streamType: String,
    @SerializedName("stream_id") val streamId: Int,
    @SerializedName("stream_icon") val streamIcon: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5based: Double,
    @SerializedName("added") val added: String?,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("container_extension") val containerExtension: String,
    @SerializedName("custom_sid") val customSid: String?,
    @SerializedName("direct_source") val directSource: String?
)

data class SeriesDto(
    @SerializedName("num") val num: Int,
    @SerializedName("name") val name: String,
    @SerializedName("series_id") val seriesId: Int,
    @SerializedName("cover") val cover: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("rating_5based") val rating5based: Double,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("category_id") val categoryId: String?
)

data class MovieInfoDto(
    @SerializedName("info") val info: MovieDetailInfoDto?,
    @SerializedName("movie_data") val movieData: MovieDto?
)

data class MovieDetailInfoDto(
    @SerializedName("tmdb_id") val tmdbId: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("o_name") val originalName: String?,
    @SerializedName("cover_big") val coverBig: String?,
    @SerializedName("movie_image") val movieImage: String?,
    @SerializedName("releasedate") val releaseDate: String?,
    @SerializedName("episode_run_time") val episodeRunTime: String?,
    @SerializedName("youtube_trailer") val youtubeTrailer: String?,
    @SerializedName("director") val director: String?,
    @SerializedName("actors") val actors: String?,
    @SerializedName("cast") val cast: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("plot") val plot: String?,
    @SerializedName("age") val age: String?,
    @SerializedName("mpaa_rating") val mpaaRating: String?,
    @SerializedName("rating_count_kinopoisk") val ratingCountKinopoisk: Int?,
    @SerializedName("country") val country: String?,
    @SerializedName("genre") val genre: String?,
    @SerializedName("backdrop_path") val backdropPath: List<String>?,
    @SerializedName("duration_secs") val durationSecs: Int?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("rating") val rating: Double?,
    @SerializedName("enable_player") val enablePlayer: Int?,
    @SerializedName("added") val added: String?,
    @SerializedName("o_filename") val oFilename: String?
)
