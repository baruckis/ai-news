package com.baruckis.ainews.feature.news.detail

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.feature.news.domain.model.NewsError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

/** Pure state-transition tests for [ArticleDetailReducer] — no mocks, no coroutines. */
class ArticleDetailReducerTest {
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

    @Test
    fun `loading records the requested id and clears a previous error`() {
        val previous = ArticleDetailState(isLoading = false, error = NewsError.Network)

        val state = ArticleDetailReducer.loading(previous, id = "42")

        assertEquals("42", state.articleId)
        assertTrue(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `success replaces the article and clears progress and error`() {
        val previous = ArticleDetailState(articleId = "42", isLoading = true, error = NewsError.Unknown)

        val state = ArticleDetailReducer.success(previous, article)

        assertEquals(article, state.article)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `failure records the error and keeps an already-loaded article`() {
        val previous = ArticleDetailState(articleId = "42", article = article, isLoading = true)

        val state = ArticleDetailReducer.failure(previous, NewsError.Network)

        assertEquals(NewsError.Network, state.error)
        assertEquals(article, state.article)
        assertFalse(state.isLoading)
    }
}
