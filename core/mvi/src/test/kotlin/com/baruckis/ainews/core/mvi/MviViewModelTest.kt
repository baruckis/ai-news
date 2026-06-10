package com.baruckis.ainews.core.mvi

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/** Exercises the [MviViewModel] base through a tiny counter subclass: state and effect paths. */
@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {
    private data class CounterState(
        val count: Int = 0,
    ) : UiState

    private sealed interface CounterIntent : Intent {
        data object Increment : CounterIntent

        data object ReportCount : CounterIntent
    }

    private sealed interface CounterEffect : Effect {
        data class CountReported(
            val count: Int,
        ) : CounterEffect
    }

    private class CounterViewModel : MviViewModel<CounterState, CounterIntent, CounterEffect>(CounterState()) {
        override fun onIntent(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Increment -> setState { copy(count = count + 1) }
                CounterIntent.ReportCount ->
                    sendEffect(CounterEffect.CountReported(currentState.count))
            }
        }
    }

    @BeforeEach
    fun setUp() {
        // viewModelScope launches on Dispatchers.Main, which has no implementation in JVM tests.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state starts with the initial value`() =
        runTest {
            val viewModel = CounterViewModel()

            assertEquals(CounterState(count = 0), viewModel.state.value)
        }

    @Test
    fun `setState reduces the current state and publishes it`() =
        runTest {
            val viewModel = CounterViewModel()

            viewModel.state.test {
                assertEquals(CounterState(count = 0), awaitItem())

                viewModel.onIntent(CounterIntent.Increment)
                assertEquals(CounterState(count = 1), awaitItem())

                viewModel.onIntent(CounterIntent.Increment)
                assertEquals(CounterState(count = 2), awaitItem())
            }
        }

    @Test
    fun `sendEffect emits to the effects flow with the current state visible`() =
        runTest {
            val viewModel = CounterViewModel()
            viewModel.onIntent(CounterIntent.Increment)

            viewModel.effects.test {
                viewModel.onIntent(CounterIntent.ReportCount)

                // currentState inside the handler saw the already-incremented value.
                assertEquals(CounterEffect.CountReported(count = 1), awaitItem())
            }
        }

    @Test
    fun `effects are buffered while uncollected and delivered exactly once`() =
        runTest {
            val viewModel = CounterViewModel()

            // Sent before anyone collects — must not be lost.
            viewModel.onIntent(CounterIntent.ReportCount)
            viewModel.onIntent(CounterIntent.Increment)
            viewModel.onIntent(CounterIntent.ReportCount)

            viewModel.effects.test {
                assertEquals(CounterEffect.CountReported(count = 0), awaitItem())
                assertEquals(CounterEffect.CountReported(count = 1), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            // A later collector does not see already-consumed effects (no replay).
            viewModel.effects.test {
                expectNoEvents()
                cancel()
            }
        }
}
