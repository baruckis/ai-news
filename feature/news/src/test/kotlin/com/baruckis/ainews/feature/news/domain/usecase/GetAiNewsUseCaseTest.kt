package com.baruckis.ainews.feature.news.domain.usecase

import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.data.NewsRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

/** Verifies [GetAiNewsUseCase] delegates to the repository and passes results through. */
class GetAiNewsUseCaseTest {
    private val repository = mockk<NewsRepository>()
    private val useCase = GetAiNewsUseCase(repository)

    private val summaries =
        listOf(
            ArticleSummary(
                id = "article-1",
                title = "AI breakthrough announced",
                description = null,
                imageUrl = null,
                sourceName = "TechCrunch",
                publishedAt = Instant.parse("2026-06-01T12:00:00Z"),
            ),
        )

    @Test
    fun `invoke returns the repository success and defaults to a cached read`() =
        runTest {
            coEvery { repository.getAiNews(forceRefresh = false) } returns RequestResult.Success(summaries)

            val result = useCase()

            assertEquals(RequestResult.Success(summaries), result)
            coVerify(exactly = 1) { repository.getAiNews(forceRefresh = false) }
        }

    @Test
    fun `invoke passes forceRefresh through to the repository`() =
        runTest {
            coEvery { repository.getAiNews(forceRefresh = true) } returns RequestResult.Success(summaries)

            useCase(forceRefresh = true)

            coVerify(exactly = 1) { repository.getAiNews(forceRefresh = true) }
        }

    @Test
    fun `invoke passes the repository error through unchanged`() =
        runTest {
            val error = RequestResult.Error(IllegalStateException("boom"))
            coEvery { repository.getAiNews(forceRefresh = false) } returns error

            assertEquals(error, useCase())
        }
}
