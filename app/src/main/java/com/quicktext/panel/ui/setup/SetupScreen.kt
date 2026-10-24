package com.quicktext.panel.ui.setup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicktext.panel.data.repository.ThemeMode
import com.quicktext.panel.service.FloatingPanelService
import com.quicktext.panel.ui.components.ScreenHeader
import com.quicktext.panel.ui.components.SectionCard

@Composable
fun SetupScreen(vm: SettingsViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val running by FloatingPanelService.running.collectAsStateWithLifecycle()
    var explain by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("Setup", "Floating panel, keyboard & appearance")

        SectionCard("Appearance") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = theme == mode,
                        onClick = { vm.setThemeMode(mode) },
                        label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    )
                }
            }
        }

        SectionCard("Floating panel") {
            Text(
                "A small movable bubble that floats over other apps. Open it, search, and tap a template to copy it — then paste it yourself. Nothing is typed or sent for you.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (running) "Panel is on" else "Panel is off", Modifier.weight(1f))
                Switch(
                    checked = running,
                    onCheckedChange = { on ->
                        when {
                            !on -> FloatingPanelService.stop(context)
                            Settings.canDrawOverlays(context) -> FloatingPanelService.start(context)
                            else -> explain = true
                        }
                    },
                )
            }
            if (running) {
                OutlinedButton(onClick = { FloatingPanelService.stop(context) }, Modifier.fillMaxWidth()) {
                    Text("Shut down panel now")
                }
            }
        }

        SectionCard("Keyboard") {
            Text(
                "1. Enable QuickText Keyboard.\n2. Switch to it from any text field.\nIt types normally and inserts a template only when you tap it. It is disabled in password fields and never records what you type.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = { context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) },
                Modifier.fillMaxWidth(),
            ) { Text("Enable keyboard") }
            OutlinedButton(
                onClick = {
                    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                },
                Modifier.fillMaxWidth(),
            ) { Text("Switch keyboard") }
        }
    }

    if (explain) {
        AlertDialog(
            onDismissRequest = { explain = false },
            title = { Text("Why overlay permission?") },
            text = {
                Text(
                    "Android requires \"Display over other apps\" for any floating window. QuickText uses it only to draw the panel you turn on. It cannot read other apps' screens, type into them, or send anything.\n\nAfter allowing it, come back and switch the panel on.",
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    explain = false
                    context.startActivity(
                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { explain = false }) { Text("Not now") } },
        )
    }
}
