package com.baruckis.ainews.bff

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class NewsDataClientTest {
    private fun clientRespondingBy(
        timeframe: String? = null,
        gnewsApiKey: String? = "gnews-key",
        requests: MutableList<Url>? = null,
        handler: (host: String) -> String,
    ): NewsDataClient {
        val engine =
            MockEngine { request ->
                requests?.add(request.url)
                respond(
                    content = ByteReadChannel(handler(request.url.host)),
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        val httpClient =
            HttpClient(engine) {
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
        return NewsDataClient(
            httpClient = httpClient,
            newsDataApiKey = "test-key",
            gnewsApiKey = gnewsApiKey,
            timeframe = timeframe,
        )
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
            val connection = clientRespondingBy { json }.fetchAiNews(null)

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
            val article = clientRespondingBy { json }.fetchAiNews(null).articles.single()

            assertNull(article.description)
            assertNull(article.content)
            assertNull(article.imageUrl)
            assertNull(article.author)
            assertEquals("reuters", article.sourceName)
        }

    @Test
    fun `normalises the free-tier content sentinel and blank content to null`() =
        runTest {
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"s1","title":"t","link":"https://news/10","pubDate":"2026-06-02 00:00:00",
                   "content":"ONLY AVAILABLE IN PAID PLANS"},
                  {"article_id":"s2","title":"t","link":"https://news/11","pubDate":"2026-06-02 00:00:00",
                   "content":"  only available in paid plans  "},
                  {"article_id":"s3","title":"t","link":"https://news/12","pubDate":"2026-06-02 00:00:00",
                   "content":"   "},
                  {"article_id":"s4","title":"t","link":"https://news/13","pubDate":"2026-06-02 00:00:00",
                   "content":"Real full text"}
                ]}
                """.trimIndent()
            val articles = clientRespondingBy { json }.fetchAiNews(null).articles

            assertNull(articles.single { it.id.value == "s1" }.content)
            assertNull(articles.single { it.id.value == "s2" }.content)
            assertNull(articles.single { it.id.value == "s3" }.content)
            assertEquals("Real full text", articles.single { it.id.value == "s4" }.content)
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
            val articles = clientRespondingBy { json }.fetchAiNews(null).articles

            assertEquals(1, articles.size)
            assertEquals("ok", articles.single().id.value)
        }

    @Test
    fun `drops articles with an unparseable pubDate`() =
        runTest {
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"bad","title":"t","link":"https://news/6","pubDate":"not-a-date"},
                  {"article_id":"none","title":"t","link":"https://news/7"}
                ]}
                """.trimIndent()
            val articles = clientRespondingBy { json }.fetchAiNews(null).articles

            assertTrue(articles.isEmpty())
        }

    @Test
    fun `forwards the cursor as the page parameter and sends the timeframe when set`() =
        runTest {
            val requests = mutableListOf<Url>()
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"x","title":"t","link":"https://news/8","pubDate":"2026-06-02 00:00:00"}
                ]}
                """.trimIndent()
            clientRespondingBy(timeframe = "24h", requests = requests) { json }.fetchAiNews("cursor-xyz")

            val newsDataUrl = requests.single { "newsdata" in it.host }
            assertEquals("cursor-xyz", newsDataUrl.parameters["page"])
            assertEquals("24h", newsDataUrl.parameters["timeframe"])
        }

    @Test
    fun `omits the page and timeframe parameters when not provided`() =
        runTest {
            val requests = mutableListOf<Url>()
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"x","title":"t","link":"https://news/8","pubDate":"2026-06-02 00:00:00"}
                ]}
                """.trimIndent()
            clientRespondingBy(requests = requests) { json }.fetchAiNews(null)

            val newsDataUrl = requests.single { "newsdata" in it.host }
            assertNull(newsDataUrl.parameters["page"])
            assertNull(newsDataUrl.parameters["timeframe"])
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

            val article = client.fetchAiNews(null).articles.single()
            assertEquals("GNews", article.sourceName)
            assertEquals("https://g/1", article.url)
            assertEquals(Instant.parse("2026-06-01T10:00:00Z"), article.publishedAt)
        }

    @Test
    fun `maps GNews articles with null optional fields and drops incomplete ones`() =
        runTest {
            val gnews =
                """
                {"articles":[
                  {"title":"minimal","url":"https://g/2","publishedAt":"2026-06-01T10:00:00Z"},
                  {"title":"no url","publishedAt":"2026-06-01T10:00:00Z"},
                  {"title":"bad date","url":"https://g/3","publishedAt":"nope"}
                ]}
                """.trimIndent()
            val client = clientRespondingBy { host -> if ("newsdata" in host) """{"status":"error"}""" else gnews }

            val article = client.fetchAiNews(null).articles.single()
            assertEquals("https://g/2", article.url)
            assertEquals("Unknown", article.sourceName)
            assertNull(article.description)
            assertNull(article.imageUrl)
            assertNull(article.author)
        }

    @Test
    fun `returns empty when NewsData fails and no GNews key is configured`() =
        runTest {
            val client = clientRespondingBy(gnewsApiKey = null) { """{"status":"error","results":[]}""" }

            val connection = client.fetchAiNews(null)
            assertTrue(connection.articles.isEmpty())
            assertNull(connection.nextPage)
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
            client.fetchAiNews(null)

            assertEquals("cached", client.fetchArticle("cached")?.id?.value)
            assertNull(client.fetchArticle("missing"))
        }

    @Test
    fun `article refreshes the cache when it is empty`() =
        runTest {
            val requests = mutableListOf<Url>()
            val json =
                """
                {"status":"success","results":[
                  {"article_id":"fresh","title":"t","link":"https://news/9","pubDate":"2026-06-02 00:00:00"}
                ]}
                """.trimIndent()
            val client = clientRespondingBy(requests = requests) { json }

            // No prior fetchAiNews call: fetchArticle must populate the cache first.
            assertEquals("fresh", client.fetchArticle("fresh")?.id?.value)
            assertTrue(requests.any { "newsdata" in it.host })
        }

    @Test
    fun `close releases the underlying http client`() =
        runTest {
            val client = clientRespondingBy { """{"status":"success","results":[]}""" }
            client.close()
        }
}
