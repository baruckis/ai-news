package com.baruckis.ainews

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import com.baruckis.ainews.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint

/** Single-activity host: renders the Navigation 3 graph inside the app theme. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppNavigation(onOpenUrl = ::openInCustomTab)
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
