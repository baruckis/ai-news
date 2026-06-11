package com.baruckis.ainews.feature.news.detail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.Article
import com.baruckis.ainews.feature.news.domain.model.NewsError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Interaction and state-rendering tests for [ArticleDetailScreen] on Robolectric: each
 * state renders its view, and taps surface as intents (or the back callback).
 */
@RunWith(RobolectricTestRunner::class)
class ArticleDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val publishedAt = Instant.parse("2026-06-01T10:00:00Z")

    // Formats with the same en-US locale pinned via robolectric.properties (qualifiers),
    // so the expectation matches the screen's Locale.getDefault() formatting on any host.
    private val expectedDate =
        DateTimeFormatter
            .ofPattern("MMM d, yyyy", Locale.US)
            .withZone(ZoneId.systemDefault())
            .format(publishedAt)

    private val article =
        Article(
            id = "42",
            title = "Detail headline",
            description = "Short description",
            content = "Full article body text",
            imageUrl = null,
            sourceName = "TechWire",
            author = "Jane Doe",
            url = "https://example.com/article",
            publishedAt = publishedAt,
        )

    private fun setScreen(
        state: ArticleDetailState,
        onIntent: (ArticleDetailIntent) -> Unit = {},
        onBack: () -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = false) {
                ArticleDetailScreen(state = state, onIntent = onIntent, onBack = onBack)
            }
        }
    }

    @Test
    fun contentState_rendersArticle() {
        setScreen(ArticleDetailState(article = article, isLoading = false))

        composeRule.onNodeWithText("Detail headline").assertIsDisplayed()
        composeRule.onNodeWithText("TechWire · $expectedDate").assertIsDisplayed()
        composeRule.onNodeWithText("Full article body text").assertIsDisplayed()
        composeRule.onNodeWithText("Read at source").assertIsDisplayed()
    }

    @Test
    fun contentState_withoutContent_fallsBackToDescription() {
        setScreen(ArticleDetailState(article = article.copy(content = null), isLoading = false))

        composeRule.onNodeWithText("Short description").assertIsDisplayed()
    }

    @Test
    fun loadingState_rendersPlaceholder() {
        // The skeleton shimmer animates forever; freeze the clock so the rule can settle.
        composeRule.mainClock.autoAdvance = false

        setScreen(ArticleDetailState(isLoading = true))

        composeRule.onNodeWithTag(ARTICLE_DETAIL_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun networkErrorState_rendersErrorViewWithRetry() {
        setScreen(ArticleDetailState(articleId = "42", isLoading = false, error = NewsError.Network))

        composeRule
            .onNodeWithText("No connection. Check your network and try again.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun unknownErrorState_rendersGenericMessage() {
        setScreen(ArticleDetailState(articleId = "42", isLoading = false, error = NewsError.Unknown))

        composeRule
            .onNodeWithText("Something went wrong while loading the article.")
            .assertIsDisplayed()
    }

    @Test
    fun retryClick_resendsLoadForTheSameArticle() {
        val intents = mutableListOf<ArticleDetailIntent>()
        setScreen(
            ArticleDetailState(articleId = "42", isLoading = false, error = NewsError.Network),
            onIntent = intents::add,
        )

        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(listOf<ArticleDetailIntent>(ArticleDetailIntent.Load("42")), intents)
    }

    @Test
    fun readAtSourceClick_emitsOpenSourceIntent() {
        val intents = mutableListOf<ArticleDetailIntent>()
        setScreen(
            ArticleDetailState(article = article, isLoading = false),
            onIntent = intents::add,
        )

        composeRule.onNodeWithText("Read at source").performClick()

        assertEquals(listOf<ArticleDetailIntent>(ArticleDetailIntent.OpenSource), intents)
    }

    @Test
    fun backClick_invokesTheBackCallback() {
        var backPresses = 0
        setScreen(
            ArticleDetailState(article = article, isLoading = false),
            onBack = { backPresses++ },
        )

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(1, backPresses)
    }
}
