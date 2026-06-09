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

    private val fakeService =
        object : NewsService {
            override suspend fun fetchAiNews(page: Int) = NewsConnection(listOf(sample), "cursor-2")

            override suspend fun fetchArticle(id: String) = sample.takeIf { it.id.value == id }
        }

    @Test
    fun `aiNews returns articles with the DateTime scalar serialized`() =
        testApplication {
            application { bffModule(fakeService) }

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
    fun `article resolves a known id and null for an unknown id`() =
        testApplication {
            application { bffModule(fakeService) }

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
            application { bffModule(fakeService) }

            val sdl = client.get("/sdl").bodyAsText()
            assertTrue(sdl.contains("scalar DateTime"), sdl)
            assertTrue(sdl.contains("aiNews"), sdl)
        }
}
