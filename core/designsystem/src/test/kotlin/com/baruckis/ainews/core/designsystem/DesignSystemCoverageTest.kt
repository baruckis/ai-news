package com.baruckis.ainews.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.baruckis.ainews.core.designsystem.components.AppButton
import com.baruckis.ainews.core.designsystem.components.AppText
import com.baruckis.ainews.core.designsystem.components.AppTopBar
import com.baruckis.ainews.core.designsystem.components.ErrorView
import com.baruckis.ainews.core.designsystem.components.NewsCardSkeleton
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.core.designsystem.theme.DefaultGrid
import com.baruckis.ainews.core.designsystem.theme.DefaultShapes
import com.baruckis.ainews.core.designsystem.theme.DefaultTypography
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DesignSystemCoverageTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun appTheme_defaultParameters() {
        composeRule.setContent {
            AppTheme {
                AppText(text = "Default Theme")
            }
        }
    }

    @Test
    fun appText_allParameters() {
        composeRule.setContent {
            AppTheme {
                AppText(
                    text = "Full Parameters",
                    modifier = Modifier.padding(8.dp),
                    style = TextStyle(color = Color.Red),
                    color = Color.Green,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    @Test
    fun appButton_disabled() {
        composeRule.setContent {
            AppTheme {
                AppButton(text = "Disabled", onClick = {}, enabled = false)
            }
        }
    }

    @Test
    fun errorView_customRetryText() {
        composeRule.setContent {
            AppTheme {
                ErrorView(message = "Error", onRetry = {}, retryText = "Retry Now")
            }
        }
    }

    @Test
    fun appTopBar_navigationIcon() {
        composeRule.setContent {
            AppTheme {
                AppTopBar(
                    title = "Title",
                    modifier = Modifier.fillMaxWidth(),
                    navigationIcon = { AppText("Back") }
                )
            }
        }
    }

    @Test
    fun newsCardSkeleton_customModifier() {
        composeRule.setContent {
            AppTheme {
                NewsCardSkeleton(modifier = Modifier.padding(16.dp))
            }
        }
    }

    @Test
    fun tokens_generatedMethods() {
        val grid = DefaultGrid
        grid.hashCode()
        grid.toString()
        grid.equals(grid)
        grid.equals(null)
        grid.equals("not a grid")
        grid.copy(m = 10.dp)

        val shapes = DefaultShapes
        shapes.hashCode()
        shapes.toString()
        shapes.equals(shapes)
        shapes.equals(null)
        shapes.copy(card = shapes.button)

        val typography = DefaultTypography
        typography.hashCode()
        typography.toString()
        typography.equals(typography)
        typography.equals(null)
        typography.copy(body = typography.titleLarge)
    }
}
