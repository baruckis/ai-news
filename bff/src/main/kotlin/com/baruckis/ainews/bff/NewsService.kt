package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.model.Article
import com.baruckis.ainews.bff.model.NewsConnection

/** Source of AI news articles for the GraphQL resolvers; abstracted so it can be faked in tests. */
interface NewsService {
    /** Returns a page of AI news for the configured timeframe. */
    suspend fun fetchAiNews(page: Int): NewsConnection

    /** Returns a single article by [id], or null if it is not in the most recent results. */
    suspend fun fetchArticle(id: String): Article?
}
