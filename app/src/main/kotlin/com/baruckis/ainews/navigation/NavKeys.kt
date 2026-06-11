package com.baruckis.ainews.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation key of the news list screen — the start destination of the app.
 *
 * Keys are serializable so [androidx.navigation3.runtime.rememberNavBackStack] can persist
 * the back stack across configuration changes and process death; no string routes exist.
 */
@Serializable
data object NewsList : NavKey

/** Navigation key of the article detail screen for the article with [id]. */
@Serializable
data class ArticleDetail(
    /** Id of the article to show. */
    val id: String,
) : NavKey
