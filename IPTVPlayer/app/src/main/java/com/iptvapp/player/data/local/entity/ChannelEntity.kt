package com.iptvapp.player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryId: String?,
    val epgChannelId: String?,
    val tvArchive: Int,
    val tvArchiveDuration: Int,
    val isFavorite: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)
