package com.baruckis.ainews.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ITERATIONS = 5

/**
 * Measures frame timing (jank) while flinging the news list, JIT-only ("before") and with
 * the baseline profile applied ("after"). The list fling is the app's hottest UI path:
 * item composition, Coil image loading and LazyColumn recycling all happen mid-gesture.
 */
@RunWith(AndroidJUnit4::class)
class NewsListScrollBenchmark {
    /** Drives the measured scroll sessions of the target app. */
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /** List fling without any ahead-of-time compilation — the "before" number. */
    @Test
    fun scrollNone() = scroll(CompilationMode.None())

    /** List fling with the baseline profile compiled in — the "after" number. */
    @Test
    fun scrollBaselineProfile() = scroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun scroll(compilationMode: CompilationMode) =
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            iterations = ITERATIONS,
            // No startupMode: with one set, the framework kills the process between the
            // setup and measure blocks (so startup can be measured inside measureBlock),
            // which would leave the flings below running against a dead app. The cold
            // launch is done explicitly in setupBlock instead, outside the measurement.
            compilationMode = compilationMode,
            setupBlock = {
                killProcess()
                pressHome()
                startActivityAndWait()
                waitForListContent()
            },
        ) {
            scrollNewsList()
        }
}
