package com.iptvapp.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.iptvapp.player.data.local.dao.AppUserDao
import com.iptvapp.player.data.local.dao.ChannelDao
import com.iptvapp.player.data.local.dao.MovieDao
import com.iptvapp.player.data.local.dao.SeriesDao
import com.iptvapp.player.data.local.entity.AppUserEntity
import com.iptvapp.player.data.local.entity.ChannelEntity
import com.iptvapp.player.data.local.entity.MovieEntity
import com.iptvapp.player.data.local.entity.SeriesEntity

@Database(
    entities = [
        ChannelEntity::class,
        MovieEntity::class,
        SeriesEntity::class,
        AppUserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun movieDao(): MovieDao
    abstract fun seriesDao(): SeriesDao
    abstract fun appUserDao(): AppUserDao
}
