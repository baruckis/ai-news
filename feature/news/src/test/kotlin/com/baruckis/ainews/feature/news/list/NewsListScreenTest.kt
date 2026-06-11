package com.baruckis.ainews.feature.news.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.feature.news.domain.model.NewsError
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Interaction and state-rendering tests for [NewsListScreen] on Robolectric: each of the
 * four states renders its view, and taps surface as intents through the single callback.
 */
@RunWith(RobolectricTestRunner::class)
class NewsListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val articles =
        listOf(
            ArticleSummary(
                id = "1",
                title = "First headline",
                description = "First description",
                imageUrl = null,
                sourceName = "TechWire",
                publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
            ),
            ArticleSummary(
                id = "2",
                title = "Second headline",
                description = null,
                imageUrl = null,
                sourceName = "AI Daily",
                publishedAt = Instant.parse("2026-06-02T10:00:00Z"),
            ),
        )

    private fun setScreen(
        state: NewsListState,
        onIntent: (NewsListIntent) -> Unit = {},
    ) {
        composeRule.setContent {
            AppTheme(darkTheme = false) {
                NewsListScreen(state = state, onIntent = onIntent)
            }
        }
    }

    @Test
    fun contentState_rendersArticles() {
        setScreen(NewsListState(articles = articles, isLoading = false))

        composeRule.onNodeWithText("First headline").assertIsDisplayed()
        composeRule.onNodeWithText("First description").assertIsDisplayed()
        composeRule.onNodeWithText("Second headline").assertIsDisplayed()
        composeRule.onNodeWithText("TechWire · Jun 1, 2026").assertIsDisplayed()
    }

    @Test
    fun loadingState_rendersSkeletons() {
        // The skeleton shimmer animates forever; freeze the clock so the rule can settle.
        composeRule.mainClock.autoAdvance = false

        setScreen(NewsListState(isLoading = true))

        composeRule.onNodeWithTag(NEWS_LIST_LOADING_TAG).assertIsDisplayed()
    }

    @Test
    fun networkErrorState_rendersErrorViewWithRetry() {
        setScreen(NewsListState(isLoading = false, error = NewsError.Network))

        composeRule
            .onNodeWithText("No connection. Check your network and try again.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun unknownErrorState_rendersGenericMessage() {
        setScreen(NewsListState(isLoading = false, error = NewsError.Unknown))

        composeRule
            .onNodeWithText("Something went wrong while loading the news.")
            .assertIsDisplayed()
    }

    @Test
    fun errorWithExistingArticles_keepsContentOnScreen() {
        setScreen(NewsListState(articles = articles, isLoading = false, error = NewsError.Network))

        composeRule.onNodeWithText("First headline").assertIsDisplayed()
        composeRule.onNodeWithText("Try again").assertDoesNotExist()
    }

    @Test
    fun emptyState_rendersEmptyView() {
        setScreen(NewsListState(isLoading = false))

        composeRule.onNodeWithText("No news yet").assertIsDisplayed()
        composeRule.onNodeWithText("Check back later for fresh AI stories.").assertIsDisplayed()
    }

    @Test
    fun articleClick_emitsArticleClickedIntent() {
        val intents = mutableListOf<NewsListIntent>()
        setScreen(NewsListState(articles = articles, isLoading = false), onIntent = intents::add)

        composeRule.onNodeWithText("Second headline").performClick()

        assertEquals(listOf<NewsListIntent>(NewsListIntent.ArticleClicked(id = "2")), intents)
    }

    @Test
    fun retryClick_emitsRetryIntent() {
        val intents = mutableListOf<NewsListIntent>()
        setScreen(
            NewsListState(isLoading = false, error = NewsError.Network),
            onIntent = intents::add,
        )

        composeRule.onNodeWithText("Try again").performClick()

        assertEquals(listOf<NewsListIntent>(NewsListIntent.Retry), intents)
    }
}
