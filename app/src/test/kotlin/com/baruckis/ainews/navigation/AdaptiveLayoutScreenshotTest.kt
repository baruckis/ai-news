package com.baruckis.ainews.navigation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.baruckis.ainews.HiltComponentActivity
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Roborazzi screenshot tests of the adaptive two-pane layout at an expanded (tablet)
 * window width, in light and dark themes. References live under `src/test/screenshots`;
 * run with `-Precord` to regenerate them.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, qualifiers = "w1280dp-h800dp")
class AdaptiveLayoutScreenshotTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    private fun snapshotTwoPane(darkTheme: Boolean) {
        composeRule.activity.setContent {
            AppTheme(darkTheme = darkTheme) {
                AppNavigation(onOpenUrl = {})
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()

        val theme = if (darkTheme) "dark" else "light"
        composeRule.onRoot().captureRoboImage(
            filePath = "src/test/screenshots/TwoPaneListDetail_$theme.png",
            roborazziOptions =
                RoborazziOptions(
                    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f),
                ),
        )
    }

    @Test
    fun light() = snapshotTwoPane(darkTheme = false)

    @Test
    fun dark() = snapshotTwoPane(darkTheme = true)
}
