package com.iptvapp.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryId: String?,
    val rating: Double,
    val containerExtension: String,
    val isFavorite: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val seriesId: Int,
    val name: String,
    val cover: String?,
    val plot: String?,
    val genre: String?,
    val rating: Double,
    val categoryId: String?,
    val isFavorite: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)
