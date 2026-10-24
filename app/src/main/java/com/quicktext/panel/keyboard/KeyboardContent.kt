package com.quicktext.panel.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quicktext.panel.data.local.BoxEntity
import com.quicktext.panel.data.local.TemplateEntity
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface KeyboardActions {
    fun commit(text: String)
    fun backspace()
    fun enter()
    fun switchKeyboard()
}

private enum class Layer { LETTERS, SYMBOLS, TEMPLATES }

private val letterRows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
private val symbolRows = listOf("1234567890", "@#\$%&-+()/", "*\"':;!?")

@Composable
fun KeyboardContent(
    passwordField: Boolean,
    boxes: List<BoxEntity>,
    templates: List<TemplateEntity>,
    actions: KeyboardActions,
) {
    var layer by remember { mutableStateOf(Layer.LETTERS) }
    var shift by remember { mutableStateOf(false) }
    var selectedBox by remember { mutableStateOf<Long?>(null) }

    Column(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 4.dp, vertical = 6.dp),
    ) {
        if (passwordField) {
            PasswordNotice(actions)
        } else when (layer) {
            Layer.LETTERS -> {
                KeyRow { letterRows[0].forEach { c -> CharKey(c, shift) { actions.commit(it); shift = false } } }
                KeyRow {
                    Spacer(Modifier.weight(0.5f))
                    letterRows[1].forEach { c -> CharKey(c, shift) { actions.commit(it); shift = false } }
                    Spacer(Modifier.weight(0.5f))
                }
                KeyRow {
                    Key("⇧", Modifier.weight(1.4f), highlighted = shift) { shift = !shift }
                    letterRows[2].forEach { c -> CharKey(c, shift) { actions.commit(it); shift = false } }
                    Key("⌫", Modifier.weight(1.4f), repeatable = true) { actions.backspace() }
                }
                BottomRow(layer, actions, onToggle = { layer = Layer.SYMBOLS }, onTemplates = { layer = Layer.TEMPLATES })
            }
            Layer.SYMBOLS -> {
                KeyRow { symbolRows[0].forEach { c -> CharKey(c, false) { actions.commit(it) } } }
                KeyRow { symbolRows[1].forEach { c -> CharKey(c, false) { actions.commit(it) } } }
                KeyRow {
                    Spacer(Modifier.weight(0.7f))
                    symbolRows[2].forEach { c -> CharKey(c, false) { actions.commit(it) } }
                    Key("⌫", Modifier.weight(1.4f), repeatable = true) { actions.backspace() }
                }
                BottomRow(layer, actions, onToggle = { layer = Layer.LETTERS }, onTemplates = { layer = Layer.TEMPLATES })
            }
            Layer.TEMPLATES -> {
                val visible = remember(templates, selectedBox) {
                    templates
                        .filter { selectedBox == null || it.boxId == selectedBox }
                        .sortedByDescending { it.favorite }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Key("ABC", Modifier.width(64.dp)) { layer = Layer.LETTERS }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(selected = selectedBox == null, onClick = { selectedBox = null }, label = { Text("All") })
                        }
                        items(boxes, key = { it.id }) { b ->
                            FilterChip(selected = selectedBox == b.id, onClick = { selectedBox = b.id }, label = { Text(b.name) })
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                LazyColumn(Modifier.heightIn(max = 170.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (visible.isEmpty()) {
                        item {
                            Text(
                                "No templates here. Add some in the QuickText app.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }
                    items(visible, key = { it.id }) { t ->
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { actions.commit(t.content) }
                                .padding(10.dp),
                        ) {
                            Text(t.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                t.content,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.PasswordNotice(actions: KeyboardActions) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "QuickText keyboard is disabled in password fields to keep them safe.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Button(onClick = actions::switchKeyboard) { Text("Switch keyboard") }
    }
}

@Composable
private fun BottomRow(layer: Layer, actions: KeyboardActions, onToggle: () -> Unit, onTemplates: () -> Unit) {
    KeyRow {
        Key(if (layer == Layer.LETTERS) "?123" else "ABC", Modifier.weight(1.4f)) { onToggle() }
        Key("\uD83D\uDCCB", Modifier.weight(1.2f)) { onTemplates() }
        Key(",", Modifier.weight(1f)) { actions.commit(",") }
        Key("space", Modifier.weight(4f)) { actions.commit(" ") }
        Key(".", Modifier.weight(1f)) { actions.commit(".") }
        Key("⏎", Modifier.weight(1.4f)) { actions.enter() }
    }
}

@Composable
private fun KeyRow(content: @Composable RowScope.() -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
private fun RowScope.CharKey(c: Char, shift: Boolean, onChar: (String) -> Unit) {
    val text = if (shift) c.uppercaseChar().toString() else c.toString()
    Key(text, Modifier.weight(1f)) { onChar(text) }
}

@Composable
private fun Key(
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    repeatable: Boolean = false,
    onClick: () -> Unit,
) {
    val current by rememberUpdatedState(onClick)
    val bg = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (highlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val base = modifier
        .height(46.dp)
        .clip(MaterialTheme.shapes.small)
        .background(bg)
    val interactive = if (repeatable) {
        base.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    current()
                    coroutineScope {
                        val repeat = launch {
                            delay(400)
                            while (true) {
                                current()
                                delay(50)
                            }
                        }
                        tryAwaitRelease()
                        repeat.cancel()
                    }
                },
            )
        }
    } else {
        base.clickable { current() }
    }
    Box(interactive, contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 18.sp, maxLines = 1)
    }
}
