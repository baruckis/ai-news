package com.baruckis.ainews.feature.news.list

import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.feature.news.domain.model.NewsError
import kotlinx.collections.immutable.toImmutableList

/**
 * Pure state transitions for the news list screen. No dependencies, no side effects —
 * each function maps an old state to a new one, so it is unit-testable without mocks.
 */
object NewsListReducer {
    /** An initial load (or retry) started: show skeletons and clear any previous error. */
    fun loading(state: NewsListState): NewsListState = state.copy(isLoading = true, error = null)

    /**
     * A pull-to-refresh started: show the refresh indicator while keeping the current
     * articles on screen — refresh never degrades visible content to skeletons.
     */
    fun refreshing(state: NewsListState): NewsListState = state.copy(isRefreshing = true, error = null)

    /**
     * A load finished with [articles]: replace the content and clear progress flags.
     * The list is copied into an immutable container here, at the domain→UI boundary,
     * so the UI state stays stable for Compose.
     */
    fun success(
        state: NewsListState,
        articles: List<ArticleSummary>,
    ): NewsListState =
        state.copy(
            isLoading = false,
            isRefreshing = false,
            articles = articles.toImmutableList(),
            error = null,
        )

    /** A load failed with [error]: keep already-loaded articles, clear progress flags. */
    fun failure(
        state: NewsListState,
        error: NewsError,
    ): NewsListState = state.copy(isLoading = false, isRefreshing = false, error = error)
}
