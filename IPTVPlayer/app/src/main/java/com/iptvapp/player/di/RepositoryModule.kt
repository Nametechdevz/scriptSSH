package com.iptvapp.player.di

import com.iptvapp.player.data.repository.AuthRepositoryImpl
import com.iptvapp.player.data.repository.ContentRepositoryImpl
import com.iptvapp.player.domain.repository.AuthRepository
import com.iptvapp.player.domain.repository.ContentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindContentRepository(impl: ContentRepositoryImpl): ContentRepository
}
