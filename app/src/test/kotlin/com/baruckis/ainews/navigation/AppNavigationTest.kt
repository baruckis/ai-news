package com.baruckis.ainews.navigation

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
 * Navigation integration tests for [AppNavigation] on Robolectric: real ViewModels (with a
 * fake repository bound through Hilt) drive the list and detail screens, proving the full
 * list → detail → back flow goes through the Navigation 3 back stack.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AppNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    private lateinit var backStack: NavBackStack<NavKey>
    private val openedUrls = mutableListOf<String>()

    /** Hosts [AppNavigation] with a back stack hoisted out so tests can observe it. */
    private fun setAppNavigation() {
        composeRule.activity.setContent {
            AppTheme {
                backStack = rememberNavBackStack(NewsList)
                AppNavigation(onOpenUrl = openedUrls::add, backStack = backStack)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun articleClick_addsTheDetailEntryToTheBackStack() {
        setAppNavigation()

        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf<NavKey>(NewsList, ArticleDetail(id = "1")), backStack.toList())
        // The detail entry is not just on the stack — it is rendered, with the article
        // loaded through the real ViewModel.
        composeRule.onNodeWithText("Full body 1").assertIsDisplayed()
        composeRule.onNodeWithText("Read at source").assertIsDisplayed()
    }

    @Test
    fun backFromDetail_removesTheEntryAndReturnsToTheList() {
        setAppNavigation()
        composeRule.onNodeWithText("Second headline").performClick()
        composeRule.waitForIdle()
        assertEquals(listOf<NavKey>(NewsList, ArticleDetail(id = "2")), backStack.toList())

        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf<NavKey>(NewsList), backStack.toList())
        composeRule.onNodeWithText("First headline").assertIsDisplayed()
    }

    @Test
    fun readAtSource_passesTheArticleUrlToTheActivityLayer() {
        setAppNavigation()
        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Read at source").performClick()
        composeRule.waitForIdle()

        assertEquals(listOf("https://example.com/articles/1"), openedUrls)
    }
}
