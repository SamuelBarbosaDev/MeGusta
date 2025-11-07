package com.agiotagemltda.megusta.di

import android.content.Context
import com.agiotagemltda.megusta.data.local.AppDatabase
import com.agiotagemltda.megusta.data.local.entity.PostEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

}