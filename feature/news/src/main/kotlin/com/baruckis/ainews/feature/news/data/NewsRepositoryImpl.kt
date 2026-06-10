package com.baruckis.ainews.feature.news.data

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.GqlApiLayer
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.core.network.graphql.GetAiNewsQuery
import com.baruckis.ainews.core.network.graphql.GetArticleQuery
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import javax.inject.Inject

/**
 * [NewsRepository] backed by the GraphQL BFF through [GqlApiLayer].
 *
 * Executes the generated operations and maps their data with [NewsMapper] inside the
 * `transform` step, so Apollo types never leave this class; a `transform` that throws
 * (e.g. a missing article) comes back as [RequestResult.Error] per the api-layer contract.
 */
class NewsRepositoryImpl
    @Inject
    constructor(
        private val api: GqlApiLayer,
        private val mapper: NewsMapper,
    ) : NewsRepository {
        override suspend fun getAiNews(forceRefresh: Boolean): RequestResult<List<ArticleSummary>> =
            api.query(GetAiNewsQuery(cursor = null), forceRefresh) { data ->
                data.aiNews.articles.map { mapper.toSummary(it.articleListItem) }
            }

        override suspend fun getArticle(id: String): RequestResult<Article> =
            api.query(GetArticleQuery(id = id)) { data ->
                mapper.toArticle(requireNotNull(data.article) { "Article $id was not found" })
            }
    }
