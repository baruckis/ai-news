package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.model.Article
import com.baruckis.ainews.bff.model.NewsConnection
import java.io.Closeable

/** Source of AI news articles for the GraphQL resolvers; abstracted so it can be faked in tests. */
interface NewsService : Closeable {
    /**
     * Returns a page of AI news for the configured timeframe. [cursor] is the opaque
     * [NewsConnection.nextPage] token from a previous result, or null for the first page.
     */
    suspend fun fetchAiNews(cursor: String?): NewsConnection

    /** Returns a single article by [id], or null if it is not in the most recent results. */
    suspend fun fetchArticle(id: String): Article?
}
