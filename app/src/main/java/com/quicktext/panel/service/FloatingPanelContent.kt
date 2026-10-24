package com.quicktext.panel.service

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.quicktext.panel.data.repository.SettingsRepository
import com.quicktext.panel.data.repository.TemplateRepository
import com.quicktext.panel.data.repository.ThemeMode
import com.quicktext.panel.ui.templates.TemplateFilter
import com.quicktext.panel.ui.templates.applyFilter
import com.quicktext.panel.ui.theme.QuickTextTheme

@Composable
fun FloatingPanelContent(
    repository: TemplateRepository,
    settings: SettingsRepository,
    onDrag: (Float, Float) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onCopy: (String) -> Unit,
    onClose: () -> Unit,
) {
    val mode by remember { settings.themeMode }.collectAsState(initial = ThemeMode.DARK)
    val all by remember { repository.observeTemplates() }.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }

    LaunchedEffect(expanded) { onExpandedChange(expanded) }

    QuickTextTheme(mode) {
        if (!expanded) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .pointerInput(Unit) { detectTapGestures(onTap = { expanded = true }) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            onDrag(drag.x, drag.y)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("Q", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
            }
        } else {
            val visible = applyFilter(all, TemplateFilter.All, query).sortedByDescending { it.favorite }
            Surface(
                Modifier.width(290.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, drag ->
                                    change.consume()
                                    onDrag(drag.x, drag.y)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "QuickText  ⠿",
                            Modifier
                                .weight(1f)
                                .padding(start = 6.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        IconButton(onClick = { expanded = false }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse")
                        }
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close panel")
                        }
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Search") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LazyColumn(
                        Modifier.heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (visible.isEmpty()) {
                            item { Text("No templates found.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                        items(visible, key = { it.id }) { t ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { onCopy(t.content) }
                                    .padding(10.dp),
                            ) {
                                Text(t.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    t.content,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Text(
                        "Tap a template to copy it, then paste.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
