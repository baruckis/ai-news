package com.baruckis.ainews.feature.news.list

import androidx.compose.runtime.Immutable
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.mvi.UiState
import com.baruckis.ainews.feature.news.domain.model.NewsError
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Single source of truth for the news list screen. Starts in the loading state so the
 * first frame shows skeletons instead of flashing an empty list.
 *
 * [Immutable] (plus the [ImmutableList] article container) lets the Compose compiler
 * treat the whole state as stable, so composables taking it as a parameter can skip
 * recomposition when the state instance has not changed.
 */
@Immutable
data class NewsListState(
    /** Articles currently shown in the list; empty until the first successful load. */
    val articles: ImmutableList<ArticleSummary> = persistentListOf(),
    /** True while the initial load (or a retry) is in flight; drives the skeleton list. */
    val isLoading: Boolean = true,
    /** True while a pull-to-refresh is in flight; drives the refresh indicator only. */
    val isRefreshing: Boolean = false,
    /** Failure of the last load, or null when the last load succeeded (or is running). */
    val error: NewsError? = null,
) : UiState {
    /**
     * True when a load finished cleanly but returned no articles; drives the empty view.
     * Also false while refreshing, so the empty view and the refresh indicator never
     * compete on screen.
     */
    val isEmpty: Boolean get() = !isLoading && !isRefreshing && error == null && articles.isEmpty()
}
