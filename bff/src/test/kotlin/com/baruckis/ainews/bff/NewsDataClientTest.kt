package com.baruckis.ainews.bff

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

class NewsDataClientTest {
    private fun clientRespondingBy(handler: (host: String) -> String): NewsDataClient {
        val engine =
            MockEngine { request ->
                respond(
                    content = ByteReadChannel(handler(request.url.host)),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val httpClient =
            HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        return NewsDataClient(httpClient, newsDataApiKey = "test-key", gnewsApiKey = "gnews-key")
    }

    @Test
    fun `maps NewsData fields and parses the pubDate as UTC`() =
        runTest {
            val json =
                """
                {"status":"success","nextPage":"cursor-2","results":[
                  {"article_id":"a1","title":"AI breakthrough","link":"https://news/1",
                   "description":"d","content":"c","pubDate":"2026-06-01 12:30:00",
                   "image_url":"https://img/1","source_id":"tc","source_name":"TechCrunch",
                   "creator":["Jane Doe"]}
                ]}
                """.trimIndent()
            val connection = clientRespondingBy { json }.fetchAiNews(1)

            val article = connection.articles.single()
            assertEquals("a1", article.id.value)
            assertEquals("AI breakthrough", article.title)
            assertEquals("TechCrunch", article.sourceName)
            assertEquals("Jane Doe", article.author)
            assertEquals("https://news/1", article.url)
            assertEquals(Instant.parse("2026-06-01T12:30:00Z"), article.publishedAt)
            assertEquals("cursor-2", connection.nextPage)
        }

    @Test
    fun `maps null optional fields to null and falls back to source_id`() =
        runTest {
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"a2","title":"t","link":"https://news/2",
                   "pubDate":"2026-06-02 00:00:00","source_id":"reuters"}
                ]}
                """.trimIndent()
            val article = clientRespondingBy { json }.fetchAiNews(1).articles.single()

            assertNull(article.description)
            assertNull(article.content)
            assertNull(article.imageUrl)
            assertNull(article.author)
            assertEquals("reuters", article.sourceName)
        }

    @Test
    fun `drops articles missing required id, title or url`() =
        runTest {
            val json =
                """
                {"status":"success","results":[
                  {"title":"no id","link":"https://news/3","pubDate":"2026-06-02 00:00:00"},
                  {"article_id":"ok","title":"valid","link":"https://news/4","pubDate":"2026-06-02 00:00:00"}
                ]}
                """.trimIndent()
            val articles = clientRespondingBy { json }.fetchAiNews(1).articles

            assertEquals(1, articles.size)
            assertEquals("ok", articles.single().id.value)
        }

    @Test
    fun `falls back to GNews when NewsData reports an error`() =
        runTest {
            val newsDataError = """{"status":"error","results":[]}"""
            val gnews =
                """
                {"totalArticles":1,"articles":[
                  {"title":"G article","description":"gd","content":"gc","url":"https://g/1",
                   "image":"https://g/img","publishedAt":"2026-06-01T10:00:00Z",
                   "source":{"name":"GNews","url":"https://gnews.io"}}
                ]}
                """.trimIndent()
            val client = clientRespondingBy { host -> if ("newsdata" in host) newsDataError else gnews }

            val article = client.fetchAiNews(1).articles.single()
            assertEquals("GNews", article.sourceName)
            assertEquals("https://g/1", article.url)
            assertEquals(Instant.parse("2026-06-01T10:00:00Z"), article.publishedAt)
        }

    @Test
    fun `article reuses the cached aiNews result`() =
        runTest {
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"cached","title":"t","link":"https://news/5","pubDate":"2026-06-02 00:00:00"}
                ]}
                """.trimIndent()
            val client = clientRespondingBy { json }
            client.fetchAiNews(1)

            assertEquals("cached", client.fetchArticle("cached")?.id?.value)
            assertNull(client.fetchArticle("missing"))
        }
}
