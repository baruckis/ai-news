package com.baruckis.ainews.feature.news.domain.repository

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.RequestResult

/**
 * Domain-side contract for news data: use cases and ViewModels depend on this interface,
 * never on Apollo or the data-layer implementation — the data package implements it,
 * keeping the dependency direction domain ← data.
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
