package com.bonfs.tcc.kmpchat.core.mvi

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

private data class TestState(
    val counter: Int = 0,
    val text: String = ""
) : UiState

private sealed interface TestIntent : UiIntent {
    data object Increment : TestIntent
    data class SetText(val newText: String) : TestIntent
    data class TriggerEffect(val message: String) : TestIntent
}

private sealed interface TestEffect : UiEffect {
    data class ShowToast(val message: String) : TestEffect
}

private class TestViewModel : BaseViewModel<TestState, TestIntent, TestEffect>(TestState()) {
    override fun sendIntent(intent: TestIntent) {
        when (intent) {
            is TestIntent.Increment -> {
                updateState { it.copy(counter = it.counter + 1) }
            }
            is TestIntent.SetText -> {
                updateState { it.copy(text = intent.newText) }
            }
            is TestIntent.TriggerEffect -> {
                emitEffect(TestEffect.ShowToast(intent.message))
            }
        }
    }
}

class BaseViewModelTest {

    @Test
    fun shouldEmitInitialState() = runTest {
        val viewModel = TestViewModel()
        assertEquals(0, viewModel.uiState.value.counter)
        assertEquals("", viewModel.uiState.value.text)
    }

    @Test
    fun shouldUpdateStateWhenIntentSent() = runTest {
        val viewModel = TestViewModel()
        viewModel.sendIntent(TestIntent.Increment)
        assertEquals(1, viewModel.uiState.value.counter)

        viewModel.sendIntent(TestIntent.SetText("Matrix"))
        assertEquals("Matrix", viewModel.uiState.value.text)
    }

    @Test
    fun shouldEmitSideEffectWhenTriggered() = runTest {
        val viewModel = TestViewModel()
        viewModel.sendIntent(TestIntent.TriggerEffect("Hello Effect"))
        val effect = viewModel.effect.first()
        assertEquals(TestEffect.ShowToast("Hello Effect"), effect)
    }
}
