package com.baruckis.ainews.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates `baseline-prof.txt` for :app by walking the critical user journey — startup
 * into the news list, then a fling through it — so the classes and methods on that path
 * are AOT-compiled at install time. Run via `./gradlew :app:generateBaselineProfile`; the
 * result is written to `app/src/release/generated/baselineProfiles/` and committed.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {
    /** Collects and stores the profile from the journey below. */
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    /** The journey the profile is collected from: cold start + news-list fling. */
    @Test
    fun generate() =
        baselineProfileRule.collect(packageName = TARGET_PACKAGE) {
            pressHome()
            startActivityAndWait()
            waitForListContent()
            scrollNewsList()
        }
}
