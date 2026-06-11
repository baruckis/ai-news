package com.baruckis.ainews.feature.news.list

import com.baruckis.ainews.core.mvi.Intent

/** Everything the user (or the system) can ask of the news list screen. */
sealed interface NewsListIntent : Intent {
    /** Initial load of the feed; sent once when the screen appears. */
    data object Load : NewsListIntent

    /** Pull-to-refresh: reload the feed bypassing the cache. */
    data object Refresh : NewsListIntent

    /** Retry after a failed load; behaves like [Load] but is a distinct user action. */
    data object Retry : NewsListIntent

    /** The user tapped the article with [id]; answered with a navigation effect. */
    data class ArticleClicked(
        val id: String,
    ) : NewsListIntent
}
