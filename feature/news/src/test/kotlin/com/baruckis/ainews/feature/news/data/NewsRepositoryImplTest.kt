package com.baruckis.ainews.feature.news.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Query
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueError
import com.apollographql.mockserver.enqueueString
import com.baruckis.ainews.core.network.DefaultGqlApiLayer
import com.baruckis.ainews.core.network.GqlApiLayer
import com.baruckis.ainews.core.network.GraphQlOperationException
import com.baruckis.ainews.core.network.RequestResult
import com.baruckis.ainews.core.network.graphql.GetAiNewsQuery
import com.baruckis.ainews.feature.news.domain.repository.NewsRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

/**
 * Exercises [NewsRepositoryImpl] end to end against the real schema: generated operations,
 * a real HTTP mock server and the real [NewsMapper] — success and error paths.
 */
class NewsRepositoryImplTest {
    /** Runs [block] with a fresh mock server and a repository pointed at it. */
    private fun withRepository(block: suspend (MockServer, NewsRepository) -> Unit) =
        runTest {
            val server = MockServer()
            val client = ApolloClient.Builder().serverUrl(server.url()).build()
            try {
                block(server, NewsRepositoryImpl(DefaultGqlApiLayer(client), NewsMapper()))
            } finally {
                client.close()
                server.close()
            }
        }

    @Test
    fun `getAiNews returns mapped summaries on success`() =
        withRepository { server, repository ->
            server.enqueueString(AI_NEWS_RESPONSE)

            val result = repository.getAiNews(forceRefresh = false)

            assertTrue(result is RequestResult.Success)
            val articles = (result as RequestResult.Success).data
            assertEquals(2, articles.size)
            val first = articles.first()
            assertEquals("article-1", first.id)
            assertEquals("AI breakthrough announced", first.title)
            assertEquals("A short description.", first.description)
            assertEquals("https://example.com/image-1.jpg", first.imageUrl)
            assertEquals("TechCrunch", first.sourceName)
            assertEquals(Instant.parse("2026-06-01T12:00:00Z"), first.publishedAt)
            // Nullable fields survive the trip as nulls instead of failing the mapping.
            val second = articles[1]
            assertNull(second.description)
            assertNull(second.imageUrl)
        }

    @Test
    fun `getAiNews maps a transport failure to Error`() =
        withRepository { server, repository ->
            server.enqueueError(statusCode = 500)

            val result = repository.getAiNews(forceRefresh = false)

            assertTrue(result is RequestResult.Error)
            assertTrue((result as RequestResult.Error).cause is ApolloException)
        }

    @Test
    fun `getAiNews maps GraphQL errors to Error`() =
        withRepository { server, repository ->
            server.enqueueString("""{"errors":[{"message":"backend exploded"}]}""")

            val result = repository.getAiNews(forceRefresh = false)

            assertTrue(result is RequestResult.Error)
            assertTrue((result as RequestResult.Error).cause is GraphQlOperationException)
        }

    @Test
    fun `getArticle returns the mapped article on success`() =
        withRepository { server, repository ->
            server.enqueueString(ARTICLE_RESPONSE)

            val result = repository.getArticle("article-1")

            assertTrue(result is RequestResult.Success)
            val article = (result as RequestResult.Success).data
            assertEquals("article-1", article.id)
            assertEquals("AI breakthrough announced", article.title)
            assertEquals("Full article body", article.content)
            assertEquals("Jane Doe", article.author)
            assertEquals("https://example.com/article-1", article.url)
            assertEquals(Instant.parse("2026-06-01T12:00:00Z"), article.publishedAt)
        }

    @Test
    fun `getArticle returns Error when the article does not exist`() =
        withRepository { server, repository ->
            // The schema allows a null article for an unknown id; the repository must
            // surface that as an Error, never as a "successful null".
            server.enqueueString("""{"data":{"article":null}}""")

            val result = repository.getArticle("missing-id")

            assertTrue(result is RequestResult.Error)
            val cause = (result as RequestResult.Error).cause
            assertTrue(cause is IllegalArgumentException)
            assertEquals("Article missing-id was not found", cause.message)
        }

    @Test
    fun `getAiNews passes forceRefresh through to the api layer`() =
        runTest {
            val recorder = RecordingGqlApiLayer()
            val repository = NewsRepositoryImpl(recorder, NewsMapper())

            repository.getAiNews(forceRefresh = true)

            assertEquals(true, recorder.lastForceRefresh)
            assertTrue(recorder.lastQuery is GetAiNewsQuery)
            // No pagination yet: the repository always asks for the first page.
            assertNull((recorder.lastQuery as GetAiNewsQuery).cursor)
        }

    /** Records the operation and flags it receives instead of touching the network. */
    private class RecordingGqlApiLayer : GqlApiLayer {
        var lastQuery: Query<*>? = null
        var lastForceRefresh: Boolean? = null

        override suspend fun <D : Query.Data, R> query(
            query: Query<D>,
            forceRefresh: Boolean,
            transform: (D) -> R,
        ): RequestResult<R> {
            lastQuery = query
            lastForceRefresh = forceRefresh
            return RequestResult.Error(IllegalStateException("recording only"))
        }
    }

    private companion object {
        val AI_NEWS_RESPONSE =
            """
            {
              "data": {
                "aiNews": {
                  "articles": [
                    {
                      "__typename": "Article",
                      "id": "article-1",
                      "title": "AI breakthrough announced",
                      "description": "A short description.",
                      "imageUrl": "https://example.com/image-1.jpg",
                      "sourceName": "TechCrunch",
                      "publishedAt": "2026-06-01T12:00:00Z"
                    },
                    {
                      "__typename": "Article",
                      "id": "article-2",
                      "title": "Models keep getting bigger",
                      "description": null,
                      "imageUrl": null,
                      "sourceName": "The Verge",
                      "publishedAt": "2026-06-02T08:30:00Z"
                    }
                  ],
                  "nextPage": "next-cursor"
                }
              }
            }
            """.trimIndent()

        val ARTICLE_RESPONSE =
            """
            {
              "data": {
                "article": {
                  "__typename": "Article",
                  "id": "article-1",
                  "title": "AI breakthrough announced",
                  "description": "A short description.",
                  "content": "Full article body",
                  "imageUrl": "https://example.com/image-1.jpg",
                  "sourceName": "TechCrunch",
                  "author": "Jane Doe",
                  "url": "https://example.com/article-1",
                  "publishedAt": "2026-06-01T12:00:00Z"
                }
              }
            }
            """.trimIndent()
    }
}
