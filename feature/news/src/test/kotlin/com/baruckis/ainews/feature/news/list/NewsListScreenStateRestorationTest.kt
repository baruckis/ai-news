package com.baruckis.ainews.feature.news.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.ArticleSummary
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

private const val ARTICLE_COUNT = 30
private const val SCROLL_TARGET_INDEX = 25

/**
 * Verifies the list scroll position survives saved-instance-state restoration (the same
 * mechanism behind configuration changes and process death): the LazyColumn's state is
 * saveable, so the restored composition shows the same articles instead of the top.
 */
@RunWith(RobolectricTestRunner::class)
class NewsListScreenStateRestorationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val articles =
        List(ARTICLE_COUNT) { index ->
            ArticleSummary(
                id = "$index",
                title = "Headline $index",
                description = null,
                imageUrl = null,
                sourceName = "Source",
                publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
            )
        }.toImmutableList()

    @Test
    fun scrollPosition_survivesStateRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            AppTheme {
                NewsListScreen(
                    state = NewsListState(articles = articles, isLoading = false),
                    onIntent = {},
                )
            }
        }

        composeRule.onNodeWithTag(NEWS_LIST_TAG).performScrollToIndex(SCROLL_TARGET_INDEX)
        composeRule.onNodeWithText("Headline $SCROLL_TARGET_INDEX").assertIsDisplayed()

        // Dispose the composition, save its state and recompose from that saved state —
        // what the framework does on rotation or when restoring after process death.
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText("Headline $SCROLL_TARGET_INDEX").assertIsDisplayed()
    }
}
