package com.baruckis.ainews.feature.news.data

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult

/**
 * Single source of news data for the feature: use cases and ViewModels go through this
 * interface, never through Apollo or [com.baruckis.ainews.core.network.GqlApiLayer] directly.
 */
interface NewsRepository {
    /**
     * Fetches the AI news feed mapped to domain summaries.
     *
     * @param forceRefresh when true, bypasses the normalized cache and fetches from the
     *   network (pull-to-refresh); otherwise reads follow the cache-first policy
     */
    suspend fun getAiNews(forceRefresh: Boolean): RequestResult<List<ArticleSummary>>

    /**
     * Fetches the full detail of a single article by [id]; a missing article is returned
     * as [RequestResult.Error] rather than a null success.
     */
    suspend fun getArticle(id: String): RequestResult<Article>
}
