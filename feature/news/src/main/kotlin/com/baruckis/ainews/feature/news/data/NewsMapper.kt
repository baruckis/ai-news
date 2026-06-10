package com.baruckis.ainews.feature.news.data

import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.core.network.graphql.GetArticleQuery
import com.baruckis.ainews.core.network.graphql.fragment.ArticleListItem
import javax.inject.Inject

/**
 * Maps Apollo-generated query data to pure domain models, so codegen types never travel
 * above the repository.
 *
 * The `DateTime` scalar is already decoded to [java.time.Instant] by the Apollo adapter;
 * nullable GraphQL fields stay nullable on the domain side.
 */
class NewsMapper
    @Inject
    constructor() {
        /** Maps a list-item fragment to the [ArticleSummary] the list screen renders. */
        fun toSummary(item: ArticleListItem): ArticleSummary =
            ArticleSummary(
                id = item.id,
                title = item.title,
                description = item.description,
                imageUrl = item.imageUrl,
                sourceName = item.sourceName,
                publishedAt = item.publishedAt,
            )

        /** Maps a full query result to the [Article] the detail screen renders. */
        fun toArticle(article: GetArticleQuery.Article): Article =
            Article(
                id = article.id,
                title = article.title,
                description = article.description,
                content = article.content,
                imageUrl = article.imageUrl,
                sourceName = article.sourceName,
                author = article.author,
                url = article.url,
                publishedAt = article.publishedAt,
            )
    }
