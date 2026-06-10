package com.baruckis.ainews.feature.news.data

import com.baruckis.ainews.core.network.graphql.GetArticleQuery
import com.baruckis.ainews.core.network.graphql.fragment.ArticleListItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant

/** Field-by-field mapping checks for [NewsMapper]: full payloads, null fields, dates. */
class NewsMapperTest {
    private val mapper = NewsMapper()

    @Test
    fun `toSummary maps every field`() {
        val item =
            ArticleListItem(
                id = "article-1",
                title = "AI breakthrough announced",
                description = "A short description.",
                imageUrl = "https://example.com/image-1.jpg",
                sourceName = "TechCrunch",
                publishedAt = Instant.parse("2026-06-01T12:00:00Z"),
            )

        val summary = mapper.toSummary(item)

        assertEquals("article-1", summary.id)
        assertEquals("AI breakthrough announced", summary.title)
        assertEquals("A short description.", summary.description)
        assertEquals("https://example.com/image-1.jpg", summary.imageUrl)
        assertEquals("TechCrunch", summary.sourceName)
        // The DateTime scalar arrives as a decoded Instant and must pass through unchanged.
        assertEquals(Instant.parse("2026-06-01T12:00:00Z"), summary.publishedAt)
    }

    @Test
    fun `toSummary keeps nullable fields null`() {
        val item =
            ArticleListItem(
                id = "article-2",
                title = "Models keep getting bigger",
                description = null,
                imageUrl = null,
                sourceName = "The Verge",
                publishedAt = Instant.parse("2026-06-02T08:30:00Z"),
            )

        val summary = mapper.toSummary(item)

        assertNull(summary.description)
        assertNull(summary.imageUrl)
    }

    @Test
    fun `toArticle maps every field`() {
        val article =
            GetArticleQuery.Article(
                id = "article-1",
                title = "AI breakthrough announced",
                description = "A short description.",
                content = "Full article body",
                imageUrl = "https://example.com/image-1.jpg",
                sourceName = "TechCrunch",
                author = "Jane Doe",
                url = "https://example.com/article-1",
                publishedAt = Instant.parse("2026-06-01T12:00:00Z"),
            )

        val domain = mapper.toArticle(article)

        assertEquals("article-1", domain.id)
        assertEquals("AI breakthrough announced", domain.title)
        assertEquals("A short description.", domain.description)
        assertEquals("Full article body", domain.content)
        assertEquals("https://example.com/image-1.jpg", domain.imageUrl)
        assertEquals("TechCrunch", domain.sourceName)
        assertEquals("Jane Doe", domain.author)
        assertEquals("https://example.com/article-1", domain.url)
        assertEquals(Instant.parse("2026-06-01T12:00:00Z"), domain.publishedAt)
    }

    @Test
    fun `toArticle keeps nullable fields null`() {
        val article =
            GetArticleQuery.Article(
                id = "article-3",
                title = "Untitled lab note",
                description = null,
                content = null,
                imageUrl = null,
                sourceName = "Ars Technica",
                author = null,
                url = "https://example.com/article-3",
                publishedAt = Instant.parse("2026-06-03T10:00:00Z"),
            )

        val domain = mapper.toArticle(article)

        assertNull(domain.description)
        assertNull(domain.content)
        assertNull(domain.imageUrl)
        assertNull(domain.author)
    }
}
