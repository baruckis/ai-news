package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.model.Article
import com.baruckis.ainews.bff.model.NewsConnection
import com.expediagroup.graphql.generator.scalars.ID
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * [NewsService] backed by NewsData.io, with a GNews fallback on error or empty results.
 *
 * The API key, optional GNews key and timeframe are read from the environment via
 * [fromEnvironment]. The last [fetchAiNews] result is cached in memory so [fetchArticle]
 * does not spend extra upstream quota.
 *
 * [timeframe] is optional and only sent when non-blank: NewsData.io's `timeframe`
 * parameter requires a paid plan, so the free tier (which returns the latest news) is the
 * default. Set `NEWS_TIMEFRAME` only with a plan that supports it.
 */
class NewsDataClient(
    private val httpClient: HttpClient,
    private val newsDataApiKey: String,
    private val gnewsApiKey: String? = null,
    private val timeframe: String? = null,
) : NewsService {
    @Volatile
    private var lastArticles: List<Article> = emptyList()

    override suspend fun fetchAiNews(page: Int): NewsConnection {
        val connection =
            runCatching { fetchFromNewsData() }
                .getOrNull()
                ?.takeIf { it.articles.isNotEmpty() }
                ?: fetchFromGNews()
        lastArticles = connection.articles
        return connection
    }

    override suspend fun fetchArticle(id: String): Article? {
        if (lastArticles.isEmpty()) fetchAiNews(DEFAULT_PAGE)
        return lastArticles.firstOrNull { it.id.value == id }
    }

    private suspend fun fetchFromNewsData(): NewsConnection {
        val response: NewsDataResponse =
            httpClient
                .get(NEWSDATA_URL) {
                    parameter("apikey", newsDataApiKey)
                    parameter("q", QUERY)
                    parameter("category", CATEGORY)
                    parameter("language", LANGUAGE)
                    if (!timeframe.isNullOrBlank()) parameter("timeframe", timeframe)
                }.body()
        check(response.status == null || response.status == STATUS_SUCCESS) {
            "NewsData returned status=${response.status}"
        }
        return NewsConnection(
            articles = response.results.mapNotNull { it.toArticleOrNull() },
            nextPage = response.nextPage,
        )
    }

    private suspend fun fetchFromGNews(): NewsConnection {
        val key = gnewsApiKey ?: return NewsConnection(emptyList(), null)
        val response: GNewsResponse =
            httpClient
                .get(GNEWS_URL) {
                    parameter("q", QUERY)
                    parameter("lang", LANGUAGE)
                    parameter("token", key)
                }.body()
        return NewsConnection(
            articles = response.articles.mapNotNull { it.toArticleOrNull() },
            nextPage = null,
        )
    }

    private fun NewsDataArticle.toArticleOrNull(): Article? {
        val identifier = articleId ?: return null
        val headline = title ?: return null
        val articleUrl = link ?: return null
        val published = parseNewsDataInstant(pubDate) ?: return null
        return Article(
            id = ID(identifier),
            title = headline,
            description = description,
            content = content,
            imageUrl = imageUrl,
            sourceName = sourceName ?: sourceId ?: UNKNOWN_SOURCE,
            author = creator?.firstOrNull(),
            url = articleUrl,
            publishedAt = published,
        )
    }

    private fun GNewsArticle.toArticleOrNull(): Article? {
        val articleUrl = url ?: return null
        val headline = title ?: return null
        val published = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return null
        return Article(
            id = ID(articleUrl),
            title = headline,
            description = description,
            content = content,
            imageUrl = image,
            sourceName = source?.name ?: UNKNOWN_SOURCE,
            author = null,
            url = articleUrl,
            publishedAt = published,
        )
    }

    /** Companion holding upstream endpoints/parameters and the environment-based factory. */
    companion object {
        private const val NEWSDATA_URL = "https://newsdata.io/api/1/news"
        private const val GNEWS_URL = "https://gnews.io/api/v4/search"
        private const val QUERY = "artificial intelligence"
        private const val CATEGORY = "technology"
        private const val LANGUAGE = "en"
        private const val STATUS_SUCCESS = "success"
        private const val UNKNOWN_SOURCE = "Unknown"
        private const val DEFAULT_PAGE = 1

        // NewsData.io returns timestamps as "yyyy-MM-dd HH:mm:ss" in UTC.
        private val NEWSDATA_DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        private fun parseNewsDataInstant(raw: String?): Instant? =
            raw?.let {
                runCatching {
                    LocalDateTime.parse(it, NEWSDATA_DATE_FORMAT).toInstant(ZoneOffset.UTC)
                }.getOrNull()
            }

        /** Builds a client from environment variables (`NEWSDATA_KEY`, `GNEWS_KEY`, `NEWS_TIMEFRAME`). */
        fun fromEnvironment(): NewsDataClient {
            val apiKey =
                System.getenv("NEWSDATA_KEY")
                    ?: error("NEWSDATA_KEY environment variable is required")
            val httpClient =
                HttpClient(CIO) {
                    install(ContentNegotiation) {
                        json(Json { ignoreUnknownKeys = true })
                    }
                }
            return NewsDataClient(
                httpClient = httpClient,
                newsDataApiKey = apiKey,
                gnewsApiKey = System.getenv("GNEWS_KEY"),
                timeframe = System.getenv("NEWS_TIMEFRAME"),
            )
        }
    }
}
