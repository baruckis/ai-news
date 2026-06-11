package com.baruckis.ainews.feature.news.detail

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.mvi.UiState
import com.baruckis.ainews.feature.news.domain.model.NewsError

/**
 * Single source of truth for the article detail screen. Starts in the loading state so
 * the first frame shows a placeholder instead of flashing empty content.
 */
data class ArticleDetailState(
    /** Id requested by the last [ArticleDetailIntent.Load]; lets the UI retry the same article. */
    val articleId: String? = null,
    /** The loaded article, or null until the first successful load. */
    val article: Article? = null,
    /** True while a load is in flight; drives the placeholder content. */
    val isLoading: Boolean = true,
    /** Failure of the last load, or null when the last load succeeded (or is running). */
    val error: NewsError? = null,
) : UiState
