package com.agiotagemltda.megusta.di

import com.agiotagemltda.megusta.data.local.dao.PostDao
import com.agiotagemltda.megusta.data.repository.PostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule{
    @Provides
    @Singleton
    fun providePostRepository(dao: PostDao): PostRepository = PostRepository(dao)
}
