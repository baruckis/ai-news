package com.baruckis.ainews.core.network

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.apollographql.cache.normalized.memory.MemoryCacheFactory
import com.apollographql.cache.normalized.normalizedCache
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueError
import com.apollographql.mockserver.enqueueString
import com.baruckis.ainews.core.network.graphql.GetAiNewsQuery
import com.baruckis.ainews.core.network.graphql.GetArticleQuery
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

/** Exercises [DefaultGqlApiLayer] against a real HTTP mock server: success and error paths. */
class DefaultGqlApiLayerTest {
    /** Runs [block] with a fresh mock server and an api layer pointed at it. */
    private fun withApiLayer(block: suspend (MockServer, GqlApiLayer) -> Unit) =
        runTest {
            val server = MockServer()
            val client = ApolloClient.Builder().serverUrl(server.url()).build()
            try {
                block(server, DefaultGqlApiLayer(client))
            } finally {
                client.close()
                server.close()
            }
        }

    @Test
    fun `news query returns Success with transformed data`() =
        withApiLayer { server, api ->
            server.enqueueString(AI_NEWS_RESPONSE)

            val result = api.query(GetAiNewsQuery(cursor = null)) { it.aiNews }

            assertTrue(result is RequestResult.Success)
            val news = (result as RequestResult.Success).data
            assertEquals(2, news.articles.size)
            val first = news.articles.first().articleListItem
            assertEquals("article-1", first.id)
            assertEquals("AI breakthrough announced", first.title)
            assertEquals("TechCrunch", first.sourceName)
            // DateTime scalar is decoded into java.time.Instant by JavaInstantAdapter.
            assertEquals(Instant.parse("2026-06-01T12:00:00Z"), first.publishedAt)
            // Nullable fields stay null instead of failing parsing.
            val second = news.articles[1].articleListItem
            assertNull(second.description)
            assertNull(second.imageUrl)
            assertEquals("next-cursor", news.nextPage)
        }

    @Test
    fun `article query returns Success with full detail`() =
        withApiLayer { server, api ->
            server.enqueueString(ARTICLE_RESPONSE)

            val result = api.query(GetArticleQuery(id = "article-1")) { it.article }

            assertTrue(result is RequestResult.Success)
            val article = (result as RequestResult.Success).data
            assertEquals("article-1", article?.id)
            assertEquals("Full article body", article?.content)
            assertEquals("Jane Doe", article?.author)
            assertEquals("https://example.com/article-1", article?.url)
        }

    @Test
    fun `graphql errors map to Error with joined messages`() =
        withApiLayer { server, api ->
            server.enqueueString("""{"errors":[{"message":"boom"},{"message":"bang"}]}""")

            val result = api.query(GetAiNewsQuery(cursor = null)) { it.aiNews }

            assertTrue(result is RequestResult.Error)
            val cause = (result as RequestResult.Error).cause
            assertTrue(cause is GraphQlOperationException)
            assertEquals("boom, bang", cause.message)
        }

    @Test
    fun `missing data without errors maps to Error`() =
        withApiLayer { server, api ->
            server.enqueueString("""{"data":null}""")

            val result = api.query(GetAiNewsQuery(cursor = null)) { it.aiNews }

            assertTrue(result is RequestResult.Error)
            val cause = (result as RequestResult.Error).cause
            assertTrue(cause is GraphQlOperationException)
            assertEquals("No data received", cause.message)
        }

    @Test
    fun `transport failure maps to Error with the client exception`() =
        withApiLayer { server, api ->
            server.enqueueError(statusCode = 500)

            val result = api.query(GetAiNewsQuery(cursor = null)) { it.aiNews }

            assertTrue(result is RequestResult.Error)
            assertTrue((result as RequestResult.Error).cause is ApolloException)
        }

    @Test
    fun `throwing transform maps to Error instead of escaping`() =
        withApiLayer { server, api ->
            server.enqueueString(AI_NEWS_RESPONSE)

            val result = api.query(GetAiNewsQuery(cursor = null)) { error("mapper exploded") }

            assertTrue(result is RequestResult.Error)
            val cause = (result as RequestResult.Error).cause
            assertTrue(cause is IllegalStateException)
            assertEquals("mapper exploded", cause.message)
        }

    @Test
    fun `forceRefresh bypasses the cache and fetches from the network`() =
        runTest {
            val server = MockServer()
            val client =
                ApolloClient
                    .Builder()
                    .serverUrl(server.url())
                    .normalizedCache(
                        normalizedCacheFactory = MemoryCacheFactory(),
                        typePolicies = emptyMap(),
                        fieldPolicies = emptyMap(),
                    ).fetchPolicy(FetchPolicy.CacheFirst)
                    .build()
            val api = DefaultGqlApiLayer(client)
            val firstTitle: (GetAiNewsQuery.Data) -> String =
                {
                    it.aiNews.articles
                        .first()
                        .articleListItem.title
                }
            try {
                // First call populates the cache from the network.
                server.enqueueString(AI_NEWS_RESPONSE)
                val initial = api.query(GetAiNewsQuery(cursor = null), transform = firstTitle)
                assertEquals("AI breakthrough announced", (initial as RequestResult.Success).data)

                server.enqueueString(REFRESHED_RESPONSE)

                // Cache-first default: answered from the cache, the queued response is untouched.
                val cached = api.query(GetAiNewsQuery(cursor = null), transform = firstTitle)
                assertEquals("AI breakthrough announced", (cached as RequestResult.Success).data)

                // forceRefresh goes back to the network and sees the refreshed payload.
                val refreshed =
                    api.query(GetAiNewsQuery(cursor = null), forceRefresh = true, transform = firstTitle)
                assertEquals("Fresh news after refresh", (refreshed as RequestResult.Success).data)
            } finally {
                client.close()
                server.close()
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

        val REFRESHED_RESPONSE =
            """
            {
              "data": {
                "aiNews": {
                  "articles": [
                    {
                      "__typename": "Article",
                      "id": "article-1",
                      "title": "Fresh news after refresh",
                      "description": null,
                      "imageUrl": null,
                      "sourceName": "TechCrunch",
                      "publishedAt": "2026-06-03T10:00:00Z"
                    }
                  ],
                  "nextPage": null
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
