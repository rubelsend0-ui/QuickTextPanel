package com.quicktext.panel.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quicktext.panel.ui.components.ScreenHeader
import com.quicktext.panel.ui.components.SectionCard

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenHeader("Privacy", "What QuickText does and doesn't do") {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        }
        val body = MaterialTheme.typography.bodyMedium
        val muted = MaterialTheme.colorScheme.onSurfaceVariant
        SectionCard("Your data stays on your phone") {
            Text("Templates and settings are stored locally (Room database and DataStore). The app has no INTERNET permission, so it cannot upload anything.", style = body, color = muted)
        }
        SectionCard("Keyboard") {
            Text("The keyboard never records, stores, or transmits keystrokes. It is switched off in password fields. Templates are inserted only when you tap one, and the keyboard never sends messages or submits forms on its own.", style = body, color = muted)
        }
        SectionCard("Floating panel") {
            Text("Optional and off by default. It needs the \"Display over other apps\" permission only to draw its window. Tapping a template copies it to your clipboard so you can paste it yourself. You can shut it down at any time from the panel or from Setup.", style = body, color = muted)
        }
        SectionCard("Test Lab") {
            Text("Repetition tests run entirely inside the app's own preview, with hard limits on count and speed. Test Lab never controls or types into other apps.", style = body, color = muted)
        }
        SectionCard("Permissions") {
            Text("SYSTEM_ALERT_WINDOW (optional floating panel). That is the only one.", style = body, color = muted)
        }
    }
}
