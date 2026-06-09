package com.baruckis.ainews.bff.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription

/** A page of [Article]s plus an optional cursor to the next page (see plan §E). */
@GraphQLDescription("A page of articles with an optional next-page cursor.")
data class NewsConnection(
    /** Articles in this page, newest first. */
    val articles: List<Article>,
    /** Opaque cursor for the next page, or null when there are no more results. */
    val nextPage: String?,
)
