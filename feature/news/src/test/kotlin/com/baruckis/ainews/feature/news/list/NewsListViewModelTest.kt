package com.baruckis.ainews.feature.news.list

import app.cash.turbine.test
import com.apollographql.apollo.exception.ApolloNetworkException
import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import com.baruckis.ainews.feature.news.domain.usecase.GetAiNewsUseCase
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
 * Intent-to-state and intent-to-effect tests for [NewsListViewModel], driven through a
 * real [GetAiNewsUseCase] backed by a fake repository (no mocking framework needed).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsListViewModelTest {
    private val article =
        ArticleSummary(
            id = "42",
            title = "Title",
            description = "Description",
            imageUrl = null,
            sourceName = "Source",
            publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
        )

    /**
     * Records every forceRefresh value and serves a configurable feed result. Setting
     * [gate] suspends the answer until the test releases it, making in-flight states
     * (loading, refreshing) observable instead of being conflated away by StateFlow.
     */
    private class FakeNewsRepository : NewsRepository {
        var feedResult: RequestResult<List<ArticleSummary>> = RequestResult.Success(emptyList())
        var gate: CompletableDeferred<Unit>? = null
        val forceRefreshCalls = mutableListOf<Boolean>()

        override suspend fun getAiNews(forceRefresh: Boolean): RequestResult<List<ArticleSummary>> {
            forceRefreshCalls += forceRefresh
            gate?.await()
            return feedResult
        }

        override suspend fun getArticle(id: String): RequestResult<Article> = error("Not used by the list screen")
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

    private fun viewModel() = NewsListViewModel(GetAiNewsUseCase(repository))

    @Test
    fun `Load success publishes articles and ends loading`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Success(listOf(article))
            repository.gate = CompletableDeferred()

            val viewModel = viewModel()

            viewModel.state.test {
                assertTrue(awaitItem().isLoading)

                repository.gate?.complete(Unit)
                val state = awaitItem()

                assertEquals(listOf(article), state.articles)
                assertFalse(state.isLoading)
                assertNull(state.error)
            }
            assertEquals(listOf(false), repository.forceRefreshCalls)
        }

    @Test
    fun `Load failure maps a connectivity exception to the Network error`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Error(ApolloNetworkException("no route to host"))

            val viewModel = viewModel()

            val state = viewModel.state.value
            assertEquals(NewsError.Network, state.error)
            assertFalse(state.isLoading)
            assertTrue(state.articles.isEmpty())
        }

    @Test
    fun `Load failure with a network error also emits ShowOfflineSnackbar`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Error(ApolloNetworkException("offline"))

            val viewModel = viewModel()

            viewModel.effects.test {
                // The effect was buffered during init and is delivered on first collection.
                assertEquals(NewsListEffect.ShowOfflineSnackbar, awaitItem())
            }
        }

    @Test
    fun `Load failure with an unknown error emits no snackbar`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Error(IllegalStateException("mapper exploded"))

            val viewModel = viewModel()

            viewModel.effects.test {
                expectNoEvents()
            }
            assertEquals(NewsError.Unknown, viewModel.state.value.error)
        }

    @Test
    fun `Refresh keeps content visible while in flight and reloads with forceRefresh`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Success(listOf(article))
            val viewModel = viewModel()

            val updated = article.copy(id = "43", title = "Fresh title")

            viewModel.state.test {
                assertEquals(listOf(article), awaitItem().articles)

                repository.gate = CompletableDeferred()
                viewModel.onIntent(NewsListIntent.Refresh)

                val refreshing = awaitItem()
                assertTrue(refreshing.isRefreshing)
                assertFalse(refreshing.isLoading)
                assertEquals(listOf(article), refreshing.articles)

                repository.feedResult = RequestResult.Success(listOf(updated))
                repository.gate?.complete(Unit)

                val refreshed = awaitItem()
                assertFalse(refreshed.isRefreshing)
                assertEquals(listOf(updated), refreshed.articles)
            }
            assertEquals(listOf(false, true), repository.forceRefreshCalls)
        }

    @Test
    fun `Refresh failure keeps already-loaded articles on screen`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Success(listOf(article))
            val viewModel = viewModel()

            repository.feedResult = RequestResult.Error(ApolloNetworkException("offline"))
            viewModel.onIntent(NewsListIntent.Refresh)

            val state = viewModel.state.value
            assertEquals(listOf(article), state.articles)
            assertEquals(NewsError.Network, state.error)
            assertFalse(state.isRefreshing)
        }

    @Test
    fun `Retry reloads through the cache-first path`() =
        runTest(testDispatcher) {
            repository.feedResult = RequestResult.Error(IllegalStateException("boom"))
            val viewModel = viewModel()

            repository.feedResult = RequestResult.Success(listOf(article))
            viewModel.onIntent(NewsListIntent.Retry)

            assertEquals(listOf(article), viewModel.state.value.articles)
            assertNull(viewModel.state.value.error)
            assertEquals(listOf(false, false), repository.forceRefreshCalls)
        }

    @Test
    fun `ArticleClicked emits NavigateToDetail instead of navigating directly`() =
        runTest(testDispatcher) {
            val viewModel = viewModel()

            viewModel.effects.test {
                viewModel.onIntent(NewsListIntent.ArticleClicked(id = "42"))

                assertEquals(NewsListEffect.NavigateToDetail(id = "42"), awaitItem())
            }
        }
}
