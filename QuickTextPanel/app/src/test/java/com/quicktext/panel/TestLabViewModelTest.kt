package com.quicktext.panel

import com.quicktext.panel.ui.lab.TestLabLimits
import com.quicktext.panel.ui.lab.TestLabViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestLabViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun limitsAreClamped() {
        val vm = TestLabViewModel()
        vm.setRepetitions(100_000)
        vm.setIntervalMs(1)
        assertEquals(TestLabLimits.MAX_REPS, vm.state.value.repetitions)
        assertEquals(TestLabLimits.MIN_INTERVAL_MS, vm.state.value.intervalMs)
    }

    @Test fun runCompletesAndFillsPreview() {
        val vm = TestLabViewModel()
        vm.setText("hi")
        vm.setRepetitions(5)
        vm.setIntervalMs(50)
        vm.start()
        dispatcher.scheduler.advanceUntilIdle()
        val s = vm.state.value
        assertEquals(5, s.progress)
        assertFalse(s.running)
        assertEquals("hi\nhi\nhi\nhi\nhi\n", s.preview)
    }

    @Test fun stopCancelsTheRun() {
        val vm = TestLabViewModel()
        vm.setRepetitions(100)
        vm.setIntervalMs(100)
        vm.start()
        dispatcher.scheduler.advanceTimeBy(250)
        dispatcher.scheduler.runCurrent()
        vm.stop()
        dispatcher.scheduler.advanceUntilIdle()
        val s = vm.state.value
        assertFalse(s.running)
        assertTrue(s.progress in 1..99)
    }

    @Test fun blankTextDoesNotStart() {
        val vm = TestLabViewModel()
        vm.setText("   ")
        vm.start()
        assertFalse(vm.state.value.running)
    }
}
