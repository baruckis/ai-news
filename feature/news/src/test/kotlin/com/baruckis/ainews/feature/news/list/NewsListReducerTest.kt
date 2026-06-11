package com.baruckis.ainews.feature.news.list

import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.feature.news.domain.model.NewsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

/** Pure state-transition tests for [NewsListReducer] — no mocks, no coroutines. */
class NewsListReducerTest {
    private val article =
        ArticleSummary(
            id = "1",
            title = "Title",
            description = "Description",
            imageUrl = null,
            sourceName = "Source",
            publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
        )

    @Test
    fun `loading sets isLoading and clears a previous error`() {
        val previous = NewsListState(isLoading = false, error = NewsError.Network)

        val state = NewsListReducer.loading(previous)

        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `refreshing sets isRefreshing but keeps existing articles visible`() {
        val previous = NewsListState(articles = listOf(article), isLoading = false)

        val state = NewsListReducer.refreshing(previous)

        assertTrue(state.isRefreshing)
        assertFalse(state.isLoading)
        assertEquals(listOf(article), state.articles)
        assertNull(state.error)
    }

    @Test
    fun `success replaces articles and clears progress and error`() {
        val previous = NewsListState(isLoading = true, isRefreshing = true, error = NewsError.Unknown)

        val state = NewsListReducer.success(previous, listOf(article))

        assertEquals(listOf(article), state.articles)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
    }

    @Test
    fun `failure records the error and keeps already-loaded articles`() {
        val previous = NewsListState(articles = listOf(article), isLoading = true, isRefreshing = true)

        val state = NewsListReducer.failure(previous, NewsError.Network)

        assertEquals(NewsError.Network, state.error)
        assertEquals(listOf(article), state.articles)
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `isEmpty is true only after a clean load with no articles`() {
        assertTrue(NewsListState(isLoading = false).isEmpty)

        assertFalse(NewsListState(isLoading = true).isEmpty)
        assertFalse(NewsListState(isLoading = false, isRefreshing = true).isEmpty)
        assertFalse(NewsListState(isLoading = false, error = NewsError.Unknown).isEmpty)
        assertFalse(NewsListState(articles = listOf(article), isLoading = false).isEmpty)
    }
}
