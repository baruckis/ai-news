package com.baruckis.ainews.navigation

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.di.NewsModule
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.time.Instant
import javax.inject.Singleton

/** Fixed feed served by [FakeNewsRepository] so navigation tests have stable content. */
val fakeArticles =
    listOf(
        Article(
            id = "1",
            title = "First headline",
            description = "First description",
            content = "Full body 1",
            imageUrl = null,
            sourceName = "TechWire",
            author = null,
            url = "https://example.com/articles/1",
            publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
        ),
        Article(
            id = "2",
            title = "Second headline",
            description = null,
            content = "Full body 2",
            imageUrl = null,
            sourceName = "AI Daily",
            author = null,
            url = "https://example.com/articles/2",
            publishedAt = Instant.parse("2026-06-02T10:00:00Z"),
        ),
    )

/**
 * In-memory [NewsRepository] serving [fakeArticles], so the navigation integration tests
 * exercise the real ViewModels and screens without any network or GraphQL layer.
 */
class FakeNewsRepository : NewsRepository {
    override suspend fun getAiNews(forceRefresh: Boolean): RequestResult<List<ArticleSummary>> =
        RequestResult.Success(
            fakeArticles.map {
                ArticleSummary(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    imageUrl = it.imageUrl,
                    sourceName = it.sourceName,
                    publishedAt = it.publishedAt,
                )
            },
        )

    override suspend fun getArticle(id: String): RequestResult<Article> =
        fakeArticles
            .find { it.id == id }
            ?.let { RequestResult.Success(it) }
            ?: RequestResult.Error(IllegalStateException("Article $id was not found"))
}

/** Replaces the production repository binding with [FakeNewsRepository] in :app tests. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [NewsModule::class])
object FakeNewsRepositoryModule {
    /** Serves the fake repository wherever a [NewsRepository] is injected. */
    @Provides
    @Singleton
    fun provideNewsRepository(): NewsRepository = FakeNewsRepository()
}
