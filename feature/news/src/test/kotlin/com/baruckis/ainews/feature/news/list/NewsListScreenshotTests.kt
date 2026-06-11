package com.baruckis.ainews.feature.news.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.model.ArticleSummary
import com.baruckis.ainews.feature.news.domain.model.NewsError
import com.baruckis.ainews.feature.news.list.components.NewsCard
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant

/**
 * Roborazzi screenshot tests for the news list UI: every screen state plus the card, in
 * light and dark themes. References live under `src/test/screenshots`; run with
 * `-Precord` to regenerate them.
 */
@RunWith(RobolectricTestRunner::class)
abstract class NewsScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    /** Fixed sample data so captures stay pixel-identical between runs. */
    protected val screenshotArticles =
        listOf(
            ArticleSummary(
                id = "1",
                title = "AI breakthrough promises faster on-device inference for everyone",
                description =
                    "Researchers unveiled a compact model architecture that runs " +
                        "state-of-the-art inference on ordinary phones.",
                imageUrl = null,
                sourceName = "TechWire",
                publishedAt = Instant.parse("2026-06-01T10:00:00Z"),
            ),
            ArticleSummary(
                id = "2",
                title = "Open models close the gap in coding benchmarks",
                description = null,
                imageUrl = null,
                sourceName = "AI Daily",
                publishedAt = Instant.parse("2026-06-02T10:00:00Z"),
            ),
        )

    protected fun snapshot(
        name: String,
        darkTheme: Boolean,
        content: @Composable () -> Unit,
    ) {
        // The skeleton and refresh indicator animate; freezing the clock lets the rule
        // settle and captures a single, deterministic frame.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            AppTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AppTheme.colors.surfacePrimary,
                ) {
                    content()
                }
            }
        }
        val theme = if (darkTheme) "dark" else "light"
        composeRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/${name}_$theme.png",
            roborazziOptions =
                RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
                ),
        )
    }

    protected fun screenSnapshot(
        name: String,
        darkTheme: Boolean,
        state: NewsListState,
    ) = snapshot(name, darkTheme) {
        NewsListScreen(state = state, onIntent = {})
    }
}

class NewsListScreenContentScreenshotTest : NewsScreenshotTest() {
    private val state = NewsListState(articles = screenshotArticles, isLoading = false)

    @Test
    fun light() = screenSnapshot("NewsListScreen_content", darkTheme = false, state = state)

    @Test
    fun dark() = screenSnapshot("NewsListScreen_content", darkTheme = true, state = state)
}

class NewsListScreenLoadingScreenshotTest : NewsScreenshotTest() {
    private val state = NewsListState(isLoading = true)

    @Test
    fun light() = screenSnapshot("NewsListScreen_loading", darkTheme = false, state = state)

    @Test
    fun dark() = screenSnapshot("NewsListScreen_loading", darkTheme = true, state = state)
}

class NewsListScreenErrorScreenshotTest : NewsScreenshotTest() {
    private val state = NewsListState(isLoading = false, error = NewsError.Network)

    @Test
    fun light() = screenSnapshot("NewsListScreen_error", darkTheme = false, state = state)

    @Test
    fun dark() = screenSnapshot("NewsListScreen_error", darkTheme = true, state = state)
}

class NewsListScreenEmptyScreenshotTest : NewsScreenshotTest() {
    private val state = NewsListState(isLoading = false)

    @Test
    fun light() = screenSnapshot("NewsListScreen_empty", darkTheme = false, state = state)

    @Test
    fun dark() = screenSnapshot("NewsListScreen_empty", darkTheme = true, state = state)
}

class NewsCardScreenshotTest : NewsScreenshotTest() {
    @Test
    fun light() =
        snapshot("NewsCard", darkTheme = false) {
            NewsCard(
                article = screenshotArticles.first(),
                onClick = {},
                modifier = Modifier.padding(AppTheme.grid.m),
            )
        }

    @Test
    fun dark() =
        snapshot("NewsCard", darkTheme = true) {
            NewsCard(
                article = screenshotArticles.first(),
                onClick = {},
                modifier = Modifier.padding(AppTheme.grid.m),
            )
        }
}
