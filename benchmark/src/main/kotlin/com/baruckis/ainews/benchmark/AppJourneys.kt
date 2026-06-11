package com.baruckis.ainews.benchmark

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

/** Application id of the app under measurement. */
internal const val TARGET_PACKAGE = "com.baruckis.ainews"

/**
 * The article list's testTag, exposed to UiAutomator as a resource id via
 * `testTagsAsResourceId` in MainActivity. Present only once articles are rendered —
 * matching on it (instead of any scrollable) keeps the journeys from flinging the
 * loading skeletons, which are also scrollable but too short to move (and therefore
 * produce zero measurable frames).
 */
private const val NEWS_LIST_RESOURCE_ID = "news_list"

private const val CONTENT_TIMEOUT_MS = 30_000L
private const val FLING_COUNT = 3
private const val GESTURE_MARGIN_DIVISOR = 5

/** Waits until the article list is on screen with real content, failing loudly otherwise. */
internal fun MacrobenchmarkScope.waitForListContent() {
    check(device.wait(Until.hasObject(By.res(NEWS_LIST_RESOURCE_ID)), CONTENT_TIMEOUT_MS)) {
        "Article list did not render within ${CONTENT_TIMEOUT_MS}ms — did the news feed load?"
    }
}

/**
 * Flings the news list down a few screens and back up — the hot path the frame-timing
 * benchmark and the baseline profile both exercise (item composition, image loading,
 * recycling). The round trip keeps the measured window long enough for tracing to
 * capture a meaningful number of frames.
 */
internal fun MacrobenchmarkScope.scrollNewsList() {
    repeat(FLING_COUNT) { flingList(Direction.DOWN) }
    repeat(FLING_COUNT) { flingList(Direction.UP) }
}

private fun MacrobenchmarkScope.flingList(direction: Direction) {
    // Re-query each round: LazyColumn recycling invalidates the previous UiObject2
    // (a stale reference throws StaleObjectException on the second fling).
    val list =
        device.findObject(By.res(NEWS_LIST_RESOURCE_ID))
            ?: error("Article list disappeared mid-journey")
    // Keep fling gestures clear of the edge-back navigation zones.
    list.setGestureMargin(device.displayWidth / GESTURE_MARGIN_DIVISOR)
    list.fling(direction)
    device.waitForIdle()
}
