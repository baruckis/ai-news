package com.baruckis.ainews.feature.news.domain.usecase

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** Verifies [GetArticleUseCase] delegates to the repository and passes results through. */
class GetArticleUseCaseTest {
    private val repository = mockk<NewsRepository>()
    private val useCase = GetArticleUseCase(repository)

    private val article =
        Article(
            id = "article-1",
            title = "AI breakthrough announced",
            description = "A short description.",
            content = "Full article body",
            imageUrl = "https://example.com/image-1.jpg",
            sourceName = "TechCrunch",
            author = "Jane Doe",
            url = "https://example.com/article-1",
            publishedAt = Instant.parse("2026-06-01T12:00:00Z"),
        )

    @Test
    fun `invoke returns the repository success for the requested id`() =
        runTest {
            coEvery { repository.getArticle("article-1") } returns RequestResult.Success(article)

            val result = useCase("article-1")

            assertEquals(RequestResult.Success(article), result)
            coVerify(exactly = 1) { repository.getArticle("article-1") }
        }

    @Test
    fun `invoke passes the repository error through unchanged`() =
        runTest {
            val error = RequestResult.Error(IllegalStateException("boom"))
            coEvery { repository.getArticle("missing-id") } returns error

            assertEquals(error, useCase("missing-id"))
        }
}
