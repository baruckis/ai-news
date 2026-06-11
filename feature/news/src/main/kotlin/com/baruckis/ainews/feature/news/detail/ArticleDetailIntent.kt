package com.baruckis.ainews.feature.news.detail

import com.baruckis.ainews.core.mvi.Intent

/** Everything the user (or the system) can ask of the article detail screen. */
sealed interface ArticleDetailIntent : Intent {
    /** Load the article with [id]; sent when the screen appears and on retry. */
    data class Load(
        val id: String,
    ) : ArticleDetailIntent

    /** The user wants to read the article at its source; answered with an open-URL effect. */
    data object OpenSource : ArticleDetailIntent
}
