package com.baruckis.ainews.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Base class for Roborazzi screenshot tests. Renders a component inside [AppTheme] on a
 * themed surface and captures a reference image per theme under `src/test/screenshots`.
 */
@RunWith(RobolectricTestRunner::class)
abstract class ScreenshotTest {
    @get:Rule
    val composeRule = createComposeRule()

    protected fun snapshot(
        name: String,
        darkTheme: Boolean,
        content: @Composable () -> Unit,
    ) {
        // Components such as the skeleton run an infinite shimmer; freezing the clock lets
        // the rule settle and captures a single, deterministic frame.
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
            roborazziOptions = RoborazziOptions(
                compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f)
            )
        )
    }
}
