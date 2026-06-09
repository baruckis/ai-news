package com.baruckis.ainews.bff

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// REST DTOs for the upstream providers. Internal: they never appear in the GraphQL schema.

@Serializable
internal data class NewsDataResponse(
    val status: String? = null,
    val results: List<NewsDataArticle> = emptyList(),
    val nextPage: String? = null,
)

@Serializable
internal data class NewsDataArticle(
    @SerialName("article_id") val articleId: String? = null,
    val title: String? = null,
    val link: String? = null,
    val description: String? = null,
    val content: String? = null,
    @SerialName("pubDate") val pubDate: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("source_id") val sourceId: String? = null,
    @SerialName("source_name") val sourceName: String? = null,
    val creator: List<String>? = null,
)

@Serializable
internal data class GNewsResponse(
    val totalArticles: Int = 0,
    val articles: List<GNewsArticle> = emptyList(),
)

@Serializable
internal data class GNewsArticle(
    val title: String? = null,
    val description: String? = null,
    val content: String? = null,
    val url: String? = null,
    val image: String? = null,
    val publishedAt: String? = null,
    val source: GNewsSource? = null,
)

@Serializable
internal data class GNewsSource(
    val name: String? = null,
    val url: String? = null,
)
