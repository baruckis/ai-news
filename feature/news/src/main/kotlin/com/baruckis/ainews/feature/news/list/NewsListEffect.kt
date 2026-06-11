package com.baruckis.ainews.feature.news.list

import com.baruckis.ainews.core.mvi.Effect

/**
 * One-time events of the news list screen. Navigation goes through here — never a direct
 * call from the ViewModel — so the ViewModel stays platform-neutral and fully testable.
 */
sealed interface NewsListEffect : Effect {
    /** Open the detail screen for the article with [id]. */
    data class NavigateToDetail(
        val id: String,
    ) : NewsListEffect

    /** Tell the user a network failure occurred without replacing the content on screen. */
    data object ShowOfflineSnackbar : NewsListEffect
}
