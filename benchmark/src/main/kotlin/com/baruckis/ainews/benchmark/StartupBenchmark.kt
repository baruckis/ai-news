package com.baruckis.ainews.benchmark

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ITERATIONS = 10

/**
 * Measures time-to-initial-display for cold and warm starts, each in two compilation
 * states: JIT-only ("before") and with the baseline profile applied ("after"). The pair
 * of numbers per startup mode is what the README performance table reports.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {
    /** Drives the measured launches of the target app. */
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /** Cold startup without any ahead-of-time compilation — the "before" number. */
    @Test
    fun coldStartupNone() = startup(StartupMode.COLD, CompilationMode.None())

    /** Cold startup with the baseline profile compiled in — the "after" number. */
    @Test
    fun coldStartupBaselineProfile() = startup(StartupMode.COLD, CompilationMode.Partial(BaselineProfileMode.Require))

    /** Warm startup without any ahead-of-time compilation — the "before" number. */
    @Test
    fun warmStartupNone() = startup(StartupMode.WARM, CompilationMode.None())

    /** Warm startup with the baseline profile compiled in — the "after" number. */
    @Test
    fun warmStartupBaselineProfile() = startup(StartupMode.WARM, CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(
        startupMode: StartupMode,
        compilationMode: CompilationMode,
    ) = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        iterations = ITERATIONS,
        startupMode = startupMode,
        compilationMode = compilationMode,
    ) {
        pressHome()
        startActivityAndWait()
    }
}
