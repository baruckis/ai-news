package com.baruckis.ainews.bff.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.scalars.ID
import java.time.Instant

/** A single AI news article exposed by the GraphQL schema (see canonical schema, plan §E). */
@GraphQLDescription("A single AI news article.")
data class Article(
    /** Stable identifier of the article. */
    val id: ID,
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
    /** Publication instant (exposed as the `DateTime` scalar). */
    val publishedAt: Instant,
)
