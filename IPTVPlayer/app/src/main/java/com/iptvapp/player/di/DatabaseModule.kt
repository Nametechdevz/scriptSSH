package com.iptvapp.player.di

import android.content.Context
import androidx.room.Room
import com.iptvapp.player.data.local.AppDatabase
import com.iptvapp.player.data.local.dao.AppUserDao
import com.iptvapp.player.data.local.dao.ChannelDao
import com.iptvapp.player.data.local.dao.MovieDao
import com.iptvapp.player.data.local.dao.SeriesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "iptv_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()
    @Provides fun provideMovieDao(db: AppDatabase): MovieDao = db.movieDao()
    @Provides fun provideSeriesDao(db: AppDatabase): SeriesDao = db.seriesDao()
    @Provides fun provideAppUserDao(db: AppDatabase): AppUserDao = db.appUserDao()
}
