package com.quicktext.panel.ui.lab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

object TestLabLimits {
    const val MAX_REPS = 200
    const val MIN_INTERVAL_MS = 50
    const val MAX_INTERVAL_MS = 2000
    const val MAX_TEXT = 200
    const val MAX_LOG_LINES = 100

    fun clampReps(v: Int) = v.coerceIn(1, MAX_REPS)
    fun clampInterval(v: Int) = v.coerceIn(MIN_INTERVAL_MS, MAX_INTERVAL_MS)
}

data class TestLabState(
    val text: String = "Hello from QuickText \uD83D\uDC4B",
    val repetitions: Int = 10,
    val intervalMs: Int = 200,
    val running: Boolean = false,
    val progress: Int = 0,
    val preview: String = "",
    val logs: List<String> = emptyList(),
)

/**
 * Repeats text ONLY into this screen's own preview. It never touches other apps.
 */
@HiltViewModel
class TestLabViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(TestLabState())
    val state: StateFlow<TestLabState> = _state.asStateFlow()
    private var job: Job? = null

    fun setText(value: String) {
        if (!_state.value.running) _state.update { it.copy(text = value.take(TestLabLimits.MAX_TEXT)) }
    }

    fun setRepetitions(value: Int) {
        if (!_state.value.running) _state.update { it.copy(repetitions = TestLabLimits.clampReps(value)) }
    }

    fun setIntervalMs(value: Int) {
        if (!_state.value.running) _state.update { it.copy(intervalMs = TestLabLimits.clampInterval(value)) }
    }

    fun start() {
        val s = _state.value
        if (s.running) return
        if (s.text.isBlank()) {
            _state.update { it.copy(logs = appendLog(it.logs, "Enter some text first.")) }
            return
        }
        _state.update {
            it.copy(
                running = true,
                progress = 0,
                preview = "",
                logs = appendLog(emptyList(), "Started: ${s.repetitions}x every ${s.intervalMs} ms"),
            )
        }
        job = viewModelScope.launch {
            try {
                for (i in 1..s.repetitions) {
                    delay(s.intervalMs.toLong())
                    _state.update {
                        it.copy(
                            progress = i,
                            preview = it.preview + s.text + "\n",
                            logs = if (i % 10 == 0 || i == s.repetitions) {
                                appendLog(it.logs, "Printed $i of ${s.repetitions}")
                            } else {
                                it.logs
                            },
                        )
                    }
                }
                finish("Completed")
            } catch (e: CancellationException) {
                finish("Stopped by user")
                throw e
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    fun clear() {
        if (!_state.value.running) {
            _state.update { it.copy(progress = 0, preview = "", logs = emptyList()) }
        }
    }

    private fun finish(message: String) {
        _state.update { it.copy(running = false, logs = appendLog(it.logs, message)) }
    }

    private fun appendLog(logs: List<String>, message: String): List<String> {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        return (logs + "$stamp  $message").takeLast(TestLabLimits.MAX_LOG_LINES)
    }
}
