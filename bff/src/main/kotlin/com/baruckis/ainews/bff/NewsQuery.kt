package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.model.Article
import com.baruckis.ainews.bff.model.NewsConnection
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.scalars.ID
import com.expediagroup.graphql.server.operations.Query

/** Root GraphQL queries for AI news, delegating to a [NewsService]. */
class NewsQuery(
    private val newsService: NewsService,
) : Query {
    /** AI news for the configured timeframe (default one week), newest first. */
    @GraphQLDescription("AI news for the configured timeframe (default one week), newest first.")
    suspend fun aiNews(page: Int? = 1): NewsConnection = newsService.fetchAiNews(page ?: 1)

    /** A single article by its [id], or null when it is not in the latest results. */
    @GraphQLDescription("A single article by id.")
    suspend fun article(id: ID): Article? = newsService.fetchArticle(id.value)
}
