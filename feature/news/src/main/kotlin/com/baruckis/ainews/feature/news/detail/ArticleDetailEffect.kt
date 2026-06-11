package com.baruckis.ainews.feature.news.detail

import com.baruckis.ainews.core.mvi.Effect

/**
 * One-time events of the article detail screen. Opening a URL goes through here — never a
 * direct call from the ViewModel — so the ViewModel stays platform-neutral and testable.
 */
sealed interface ArticleDetailEffect : Effect {
    /** Open [url] in the browser (a Custom Tab) at the UI layer. */
    data class OpenUrl(
        val url: String,
    ) : ArticleDetailEffect
}
