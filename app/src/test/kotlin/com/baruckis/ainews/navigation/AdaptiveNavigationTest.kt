package com.baruckis.ainews.navigation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.baruckis.ainews.HiltComponentActivity
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Adaptive layout tests for [AppNavigation] at an expanded (tablet) window width: the
 * list and the detail render side by side through [TwoPaneSceneStrategy], selection
 * replaces the detail pane, and the detail pane carries no back button.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, qualifiers = "w1280dp-h800dp")
class AdaptiveNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    private lateinit var backStack: NavBackStack<NavKey>

    /** Hosts [AppNavigation] with a back stack hoisted out so tests can observe it. */
    private fun setAppNavigation() {
        composeRule.activity.setContent {
            AppTheme {
                backStack = rememberNavBackStack(NewsList)
                AppNavigation(onOpenUrl = {}, backStack = backStack)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun expandedWidth_showsTheListNextToThePlaceholderUntilAnArticleIsSelected() {
        setAppNavigation()

        // Both panes are on screen at once: the list and the no-selection placeholder.
        composeRule.onNodeWithText("First headline").assertIsDisplayed()
        composeRule.onNodeWithText("No article selected").assertIsDisplayed()
    }

    @Test
    fun selectingAnArticle_showsItsDetailNextToTheStillVisibleList() {
        setAppNavigation()

        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Full body 1").assertIsDisplayed()
        // The list did not leave the screen — its second card is still clickable.
        composeRule.onNodeWithText("Second headline").assertIsDisplayed()
        // No back button: the list stays visible, so there is nothing to go back to.
        composeRule.onAllNodesWithContentDescription("Back").assertCountEquals(0)
    }

    @Test
    fun selectingAnotherArticle_replacesTheDetailPaneInsteadOfStackingIt() {
        setAppNavigation()
        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Second headline").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Full body 2").assertIsDisplayed()
        // The previous detail entry was replaced, not buried under the new one, so back
        // returns straight to the bare list.
        assertEquals(listOf<NavKey>(NewsList, ArticleDetail(id = "2")), backStack.toList())
    }
}
