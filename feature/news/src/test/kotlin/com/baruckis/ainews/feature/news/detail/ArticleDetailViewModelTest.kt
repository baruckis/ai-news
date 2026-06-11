package com.baruckis.ainews.feature.news.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.apollographql.apollo.exception.ApolloNetworkException
import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import com.baruckis.ainews.feature.news.domain.usecase.GetArticleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Intent-to-state and intent-to-effect tests for [ArticleDetailViewModel], driven through
 * a real [GetArticleUseCase] backed by a fake repository (no mocking framework needed).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ArticleDetailViewModelTest {
    private val article =
        Article(
            id = "42",
            title = "Title",
            description = "Description",
            content = "Content",
            imageUrl = null,
            sourceName = "Source",
            author = "Author",
            url = "https://example.com/article",
            publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
        )

    /**
     * Records every requested id and serves a configurable article result. Setting [gate]
     * suspends the answer until the test releases it, making the in-flight loading state
     * observable instead of being conflated away by StateFlow.
     */
    private class FakeNewsRepository : NewsRepository {
        var articleResult: RequestResult<Article> = RequestResult.Error(IllegalStateException("unset"))
        var gate: CompletableDeferred<Unit>? = null
        val requestedIds = mutableListOf<String>()
        var cancelledCalls = 0

        override suspend fun getAiNews(forceRefresh: Boolean): RequestResult<List<ArticleSummary>> =
            error("Not used by the detail screen")

        override suspend fun getArticle(id: String): RequestResult<Article> {
            requestedIds += id
            // Snapshot on entry: a cancelled in-flight call must answer with the value it
            // started with, never with one configured for a later call.
            val result = articleResult
            try {
                gate?.await()
            } catch (cancellation: CancellationException) {
                cancelledCalls++
                throw cancellation
            }
            return result
        }
    }

    private val repository = FakeNewsRepository()

    // Shared by setMain and runTest so both run on a single TestCoroutineScheduler.
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        // viewModelScope launches on Dispatchers.Main, which has no implementation in JVM tests.
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        ArticleDetailViewModel(GetArticleUseCase(repository), savedStateHandle)

    @Test
    fun `Load success publishes the article and ends loading`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Success(article)
            repository.gate = CompletableDeferred()

            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            viewModel.state.test {
                val loading = awaitItem()
                assertTrue(loading.isLoading)
                assertEquals("42", loading.articleId)

                repository.gate?.complete(Unit)
                val state = awaitItem()

                assertEquals(article, state.article)
                assertFalse(state.isLoading)
                assertNull(state.error)
            }
            assertEquals(listOf("42"), repository.requestedIds)
        }

    @Test
    fun `Load failure maps a connectivity exception to the Network error`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Error(ApolloNetworkException("no route to host"))

            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            val state = viewModel.state.value
            assertEquals(NewsError.Network, state.error)
            assertFalse(state.isLoading)
            assertNull(state.article)
        }

    @Test
    fun `Load failure with any other exception maps to the Unknown error`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Error(IllegalStateException("mapper exploded"))

            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            assertEquals(NewsError.Unknown, viewModel.state.value.error)
        }

    @Test
    fun `Load for the already-shown article skips the repeat round trip`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Success(article)

            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            assertEquals(listOf("42"), repository.requestedIds)
            assertEquals(article, viewModel.state.value.article)
        }

    @Test
    fun `Load after a failure retries the same article`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Error(ApolloNetworkException("offline"))
            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))
            assertEquals(NewsError.Network, viewModel.state.value.error)

            repository.articleResult = RequestResult.Success(article)
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            assertEquals(article, viewModel.state.value.article)
            assertNull(viewModel.state.value.error)
            assertEquals(listOf("42", "42"), repository.requestedIds)
        }

    @Test
    fun `Load for a different article cancels an in-flight load`() =
        runTest(testDispatcher) {
            val stale = article.copy(id = "stale", title = "Stale headline")
            repository.articleResult = RequestResult.Success(stale)
            repository.gate = CompletableDeferred()

            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("stale")) // suspends on the gate
            val staleGate = repository.gate ?: error("gate was set above")

            val fresh = article.copy(id = "fresh", title = "Fresh headline")
            repository.articleResult = RequestResult.Success(fresh)
            repository.gate = CompletableDeferred()
            viewModel.onIntent(ArticleDetailIntent.Load("fresh"))

            // Single-flight: starting the second load cancelled the in-flight first one.
            assertEquals(1, repository.cancelledCalls)

            repository.gate?.complete(Unit)
            assertEquals(fresh, viewModel.state.value.article)

            // Releasing the cancelled load's gate must not resurrect its stale result.
            staleGate.complete(Unit)
            assertEquals(fresh, viewModel.state.value.article)
            assertEquals(listOf("stale", "fresh"), repository.requestedIds)
        }

    @Test
    fun `a Load persists the article id so a recreated ViewModel reloads it on init`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Success(article)
            // Shared handle: the framework hands the same saved state to the ViewModel
            // recreated after process death.
            val savedStateHandle = SavedStateHandle()

            viewModel(savedStateHandle).onIntent(ArticleDetailIntent.Load("42"))
            val recreated = viewModel(savedStateHandle)

            // The recreated instance restored the id and loaded without any intent.
            assertEquals(listOf("42", "42"), repository.requestedIds)
            assertEquals(article, recreated.state.value.article)
        }

    @Test
    fun `a ViewModel without saved state stays in the initial loading state`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            assertTrue(viewModel.state.value.isLoading)
            assertEquals(emptyList<String>(), repository.requestedIds)
        }

    @Test
    fun `OpenSource emits OpenUrl with the article url instead of opening it`() =
        runTest(testDispatcher) {
            repository.articleResult = RequestResult.Success(article)
            val viewModel = viewModel()
            viewModel.onIntent(ArticleDetailIntent.Load("42"))

            viewModel.effects.test {
                viewModel.onIntent(ArticleDetailIntent.OpenSource)

                assertEquals(ArticleDetailEffect.OpenUrl(article.url), awaitItem())
            }
        }

    @Test
    fun `OpenSource before the article is loaded emits nothing`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effects.test {
                viewModel.onIntent(ArticleDetailIntent.OpenSource)

                expectNoEvents()
            }
        }
}
