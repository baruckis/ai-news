package com.baruckis.ainews.feature.news.list

import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.mvi.UiState
import com.baruckis.ainews.feature.news.domain.model.NewsError

/**
 * Single source of truth for the news list screen. Starts in the loading state so the
 * first frame shows skeletons instead of flashing an empty list.
 */
data class NewsListState(
    /** Articles currently shown in the list; empty until the first successful load. */
    val articles: List<ArticleSummary> = emptyList(),
    /** True while the initial load (or a retry) is in flight; drives the skeleton list. */
    val isLoading: Boolean = true,
    /** True while a pull-to-refresh is in flight; drives the refresh indicator only. */
    val isRefreshing: Boolean = false,
    /** Failure of the last load, or null when the last load succeeded (or is running). */
    val error: NewsError? = null,
) : UiState {
    /** True when a load finished cleanly but returned no articles; drives the empty view. */
    val isEmpty: Boolean get() = !isLoading && error == null && articles.isEmpty()
}
