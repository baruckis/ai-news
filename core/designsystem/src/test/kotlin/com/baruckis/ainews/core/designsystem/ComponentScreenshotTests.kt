package com.baruckis.ainews.core.designsystem

import com.baruckis.ainews.core.designsystem.components.AppButton
import com.baruckis.ainews.core.designsystem.components.AppText
import com.baruckis.ainews.core.designsystem.components.AppTopBar
import com.baruckis.ainews.core.designsystem.components.EmptyView
import com.baruckis.ainews.core.designsystem.components.ErrorView
import com.baruckis.ainews.core.designsystem.components.NewsCardSkeleton
import com.baruckis.ainews.core.designsystem.theme.AppTheme
import org.junit.Test

class AppTextScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("AppText", darkTheme = false) {
            AppText(text = "The quick brown fox", style = AppTheme.typography.titleMedium)
        }

    @Test
    fun dark() =
        snapshot("AppText", darkTheme = true) {
            AppText(text = "The quick brown fox", style = AppTheme.typography.titleMedium)
        }
}

class AppButtonScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("AppButton", darkTheme = false) {
            AppButton(text = "Try again", onClick = {})
        }

    @Test
    fun dark() =
        snapshot("AppButton", darkTheme = true) {
            AppButton(text = "Try again", onClick = {})
        }
}

class AppTopBarScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("AppTopBar", darkTheme = false) {
            AppTopBar(title = "AI News")
        }

    @Test
    fun dark() =
        snapshot("AppTopBar", darkTheme = true) {
            AppTopBar(title = "AI News")
        }
}

class NewsCardSkeletonScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("NewsCardSkeleton", darkTheme = false) {
            NewsCardSkeleton()
        }

    @Test
    fun dark() =
        snapshot("NewsCardSkeleton", darkTheme = true) {
            NewsCardSkeleton()
        }
}

class ErrorViewScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("ErrorView", darkTheme = false) {
            ErrorView(message = "Something went wrong while loading the news.", onRetry = {})
        }

    @Test
    fun dark() =
        snapshot("ErrorView", darkTheme = true) {
            ErrorView(message = "Something went wrong while loading the news.", onRetry = {})
        }
}

class EmptyViewScreenshotTest : ScreenshotTest() {
    @Test
    fun light() =
        snapshot("EmptyView", darkTheme = false) {
            EmptyView(title = "No news yet", message = "Check back later for fresh AI stories.")
        }

    @Test
    fun dark() =
        snapshot("EmptyView", darkTheme = true) {
            EmptyView(title = "No news yet", message = "Check back later for fresh AI stories.")
        }
}
