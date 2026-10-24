package com.quicktext.panel.ui.lab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicktext.panel.ui.components.ScreenHeader
import com.quicktext.panel.ui.components.SectionCard
import kotlin.math.roundToInt

@Composable
fun TestLabScreen(vm: TestLabViewModel = hiltViewModel()) {
    val s by vm.state.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("Test Lab", "Repeats text only inside this screen's preview")

        SectionCard("Configuration") {
            OutlinedTextField(
                value = s.text,
                onValueChange = vm::setText,
                label = { Text("Text to repeat") },
                enabled = !s.running,
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            Text("Repetitions: ${s.repetitions}")
            Slider(
                value = s.repetitions.toFloat(),
                onValueChange = { vm.setRepetitions(it.roundToInt()) },
                valueRange = 1f..TestLabLimits.MAX_REPS.toFloat(),
                enabled = !s.running,
            )
            Text("Interval: ${s.intervalMs} ms")
            Slider(
                value = s.intervalMs.toFloat(),
                onValueChange = { vm.setIntervalMs(it.roundToInt()) },
                valueRange = TestLabLimits.MIN_INTERVAL_MS.toFloat()..TestLabLimits.MAX_INTERVAL_MS.toFloat(),
                enabled = !s.running,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::start, enabled = !s.running, modifier = Modifier.weight(1f)) { Text("Start") }
                OutlinedButton(onClick = vm::stop, enabled = s.running, modifier = Modifier.weight(1f)) { Text("Stop") }
                TextButton(onClick = vm::clear, enabled = !s.running) { Text("Clear") }
            }
        }

        SectionCard("Progress") {
            LinearProgressIndicator(
                progress = { if (s.repetitions == 0) 0f else s.progress.toFloat() / s.repetitions },
                modifier = Modifier.fillMaxWidth(),
            )
            Text("${s.progress} / ${s.repetitions}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        SectionCard("Preview") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp, max = 200.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    s.preview.ifEmpty { "Nothing yet. Press Start." },
                    color = if (s.preview.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        SectionCard("Log") {
            if (s.logs.isEmpty()) {
                Text("No events yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                s.logs.forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
