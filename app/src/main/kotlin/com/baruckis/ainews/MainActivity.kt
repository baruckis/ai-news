package com.baruckis.ainews

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host: renders the Navigation 3 graph inside the app theme. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Compat splash (Core SplashScreen API): shows the branded icon until the first
        // frame is drawn, then hands over to the post-splash theme. Must precede super.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppNavigation(
                    onOpenUrl = ::openInCustomTab,
                    // UiAutomator (the Macrobenchmark journeys) can target Compose nodes
                    // only through resource ids; this exposes every testTag as one.
                    modifier = Modifier.semantics { testTagsAsResourceId = true },
                )
            }
        }
    }

    /**
     * Opens [url] in a Chrome Custom Tab. URL opening lives at the Activity layer on
     * purpose: ViewModels only emit an open-URL effect and stay free of Android intents.
     */
    private fun openInCustomTab(url: String) {
        try {
            CustomTabsIntent.Builder().build().launchUrl(this, url.toUri())
        } catch (_: ActivityNotFoundException) {
            // No browser on the device — the action is simply unavailable.
        }
    }
}
