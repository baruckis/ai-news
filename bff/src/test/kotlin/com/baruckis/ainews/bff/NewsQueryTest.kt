package com.baruckis.ainews.bff

import com.baruckis.ainews.bff.model.Article
import com.baruckis.ainews.bff.model.NewsConnection
import com.expediagroup.graphql.generator.scalars.ID
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class NewsQueryTest {
    private val sample =
        Article(
            id = ID("a1"),
            title = "AI breakthrough",
            description = null,
            content = null,
            imageUrl = null,
            sourceName = "TechCrunch",
            author = null,
            url = "https://news/1",
            publishedAt = Instant.parse("2026-06-01T12:30:00Z"),
        )

    /** Records the cursor it was last called with and whether it was closed. */
    private class RecordingService(
        private val article: Article,
    ) : NewsService {
        var lastCursor: String? = "UNSET"
        var closed: Boolean = false

        override suspend fun fetchAiNews(cursor: String?): NewsConnection {
            lastCursor = cursor
            return NewsConnection(listOf(article), "cursor-2")
        }

        override suspend fun fetchArticle(id: String): Article? = article.takeIf { it.id.value == id }

        override fun close() {
            closed = true
        }
    }

    @Test
    fun `aiNews returns articles with the DateTime scalar serialized`() =
        testApplication {
            application { bffModule(RecordingService(sample)) }

            val response =
                client.post("/graphql") {
                    contentType(ContentType.Application.Json)
                    setBody("""{"query":"{ aiNews { articles { id title sourceName publishedAt } nextPage } }"}""")
                }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("\"id\":\"a1\""), body)
            assertTrue(body.contains("AI breakthrough"), body)
            assertTrue(body.contains("2026-06-01T12:30:00Z"), body)
            assertTrue(body.contains("\"nextPage\":\"cursor-2\""), body)
        }

    @Test
    fun `aiNews forwards the cursor argument to the service`() =
        testApplication {
            val service = RecordingService(sample)
            application { bffModule(service) }

            client.post("/graphql") {
                contentType(ContentType.Application.Json)
                setBody("""{"query":"{ aiNews(cursor: \"page-7\") { articles { id } } }"}""")
            }

            assertEquals("page-7", service.lastCursor)
        }

    @Test
    fun `aiNews without a cursor passes null to the service`() =
        testApplication {
            val service = RecordingService(sample)
            application { bffModule(service) }

            client.post("/graphql") {
                contentType(ContentType.Application.Json)
                setBody("""{"query":"{ aiNews { articles { id } } }"}""")
            }

            assertEquals(null, service.lastCursor)
        }

    @Test
    fun `article resolves a known id and null for an unknown id`() =
        testApplication {
            application { bffModule(RecordingService(sample)) }

            val found =
                client
                    .post("/graphql") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"query":"{ article(id: \"a1\") { title } }"}""")
                    }.bodyAsText()
            assertTrue(found.contains("AI breakthrough"), found)

            val missing =
                client
                    .post("/graphql") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"query":"{ article(id: \"nope\") { title } }"}""")
                    }.bodyAsText()
            assertTrue(missing.contains("\"article\":null"), missing)
        }

    @Test
    fun `SDL route exposes the schema with the DateTime scalar`() =
        testApplication {
            application { bffModule(RecordingService(sample)) }

            val sdl = client.get("/sdl").bodyAsText()
            assertTrue(sdl.contains("scalar DateTime"), sdl)
            assertTrue(sdl.contains("aiNews"), sdl)
        }

    @Test
    fun `closes the news service when the application stops`() {
        val service = RecordingService(sample)
        testApplication {
            application { bffModule(service) }
            client.get("/sdl")
        }
        assertTrue(service.closed, "expected the service to be closed on ApplicationStopped")
    }
}
