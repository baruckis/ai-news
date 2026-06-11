package com.baruckis.ainews.feature.news.detail

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.feature.news.domain.model.NewsError

/**
 * Pure state transitions for the article detail screen. No dependencies, no side effects —
 * each function maps an old state to a new one, so it is unit-testable without mocks.
 */
object ArticleDetailReducer {
    /** A load of the article with [id] started: show the placeholder, clear any previous error. */
    fun loading(
        state: ArticleDetailState,
        id: String,
    ): ArticleDetailState = state.copy(articleId = id, isLoading = true, error = null)

    /** A load finished with [article]: replace the content and clear the progress flag. */
    fun success(
        state: ArticleDetailState,
        article: Article,
    ): ArticleDetailState = state.copy(article = article, isLoading = false, error = null)

    /** A load failed with [error]: keep an already-loaded article, clear the progress flag. */
    fun failure(
        state: ArticleDetailState,
        error: NewsError,
    ): ArticleDetailState = state.copy(isLoading = false, error = error)
}
