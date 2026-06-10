package com.baruckis.ainews.core.model

import java.time.Instant

/**
 * Full detail of a single AI news article, as shown on the detail screen.
 *
 * Pure domain model: no framework types, mapped from transport DTOs by the data layer.
 */
data class Article(
    /** Stable identifier of the article. */
    val id: String,
    /** Headline of the article. */
    val title: String,
    /** Short summary, when the source provides one. */
    val description: String?,
    /** Full or partial body text, when available. */
    val content: String?,
    /** URL of the lead image, when available. */
    val imageUrl: String?,
    /** Human-readable name of the publishing source. */
    val sourceName: String,
    /** Author/creator, when provided by the source. */
    val author: String?,
    /** Canonical URL of the original article. */
    val url: String,
    /** Publication instant. */
    val publishedAt: Instant,
)
