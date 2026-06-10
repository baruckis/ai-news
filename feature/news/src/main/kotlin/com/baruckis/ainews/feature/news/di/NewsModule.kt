package com.baruckis.ainews.feature.news.di

import com.baruckis.ainews.feature.news.data.NewsRepositoryImpl
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Hilt bindings for the news feature's data layer. */
@Module
@InstallIn(SingletonComponent::class)
interface NewsModule {
    /** Serves [NewsRepositoryImpl] wherever a [NewsRepository] is injected. */
    @Binds
    @Singleton
    fun bindNewsRepository(impl: NewsRepositoryImpl): NewsRepository
}
