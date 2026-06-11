package com.baruckis.ainews.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
 * Verifies the Navigation 3 back stack survives saved-instance-state restoration (the
 * same mechanism behind configuration changes and process death): the @Serializable
 * [NavKey]s are persisted by rememberNavBackStack, so the restored composition is still
 * on the detail screen instead of falling back to the list.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class AppNavigationStateRestorationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    private lateinit var backStack: NavBackStack<NavKey>

    @Test
    fun backStackAndSelectedArticle_surviveStateRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            AppTheme {
                backStack = rememberNavBackStack(NewsList)
                AppNavigation(onOpenUrl = {}, backStack = backStack)
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("First headline").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Full body 1").assertIsDisplayed()

        // Dispose the composition, save its state and recompose from that saved state —
        // what the framework does on rotation or when restoring after process death.
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertEquals(listOf<NavKey>(NewsList, ArticleDetail(id = "1")), backStack.toList())
        composeRule.onNodeWithText("Full body 1").assertIsDisplayed()
    }
}
