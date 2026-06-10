package com.baruckis.ainews.core.model

import java.time.Instant

/**
 * Compact projection of an [Article] for list items — only the fields the list screen renders.
 */
data class ArticleSummary(
    /** Stable identifier of the article. */
    val id: String,
    /** Headline of the article. */
    val title: String,
    /** Short summary, when the source provides one. */
    val description: String?,
    /** URL of the lead image, when available. */
    val imageUrl: String?,
    /** Human-readable name of the publishing source. */
    val sourceName: String,
    /** Publication instant. */
    val publishedAt: Instant,
)
