package com.quicktext.panel.ui.templates

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quicktext.panel.data.local.BoxEntity
import com.quicktext.panel.data.local.TemplateEntity
import com.quicktext.panel.ui.components.ScreenHeader
import com.quicktext.panel.util.TextStats

sealed interface TemplateDialog {
    data object None : TemplateDialog
    data object NewBox : TemplateDialog
    data class RenameBox(val box: BoxEntity) : TemplateDialog
    data class DeleteBox(val box: BoxEntity) : TemplateDialog
    data class Editor(val template: TemplateEntity?) : TemplateDialog
    data class DeleteTemplate(val template: TemplateEntity) : TemplateDialog
}

@Composable
fun TemplatesScreen(vm: TemplatesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var dialog by remember { mutableStateOf<TemplateDialog>(TemplateDialog.None) }
    val boxNames = remember(state.boxes) { state.boxes.associate { it.id to it.name } }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { dialog = TemplateDialog.Editor(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) { Icon(Icons.Default.Add, contentDescription = "New template") }
        },
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ScreenHeader("Templates", "${state.templates.size} shown")

            OutlinedTextField(
                value = state.query,
                onValueChange = vm::setQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                placeholder = { Text("Search templates") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = state.filter == TemplateFilter.All,
                        onClick = { vm.setFilter(TemplateFilter.All) },
                        label = { Text("All") },
                    )
                }
                item {
                    FilterChip(
                        selected = state.filter == TemplateFilter.Favorites,
                        onClick = { vm.setFilter(TemplateFilter.Favorites) },
                        label = { Text("Favorites") },
                    )
                }
                items(state.boxes, key = { it.id }) { box ->
                    FilterChip(
                        selected = (state.filter as? TemplateFilter.Box)?.id == box.id,
                        onClick = { vm.setFilter(TemplateFilter.Box(box.id)) },
                        label = { Text(box.name) },
                    )
                }
                item {
                    AssistChip(onClick = { dialog = TemplateDialog.NewBox }, label = { Text("+ New box") })
                }
            }

            val f = state.filter
            if (f is TemplateFilter.Box) {
                val box = state.boxes.firstOrNull { it.id == f.id }
                if (box != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Box: ${box.name}",
                            Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = { dialog = TemplateDialog.RenameBox(box) }) { Text("Rename") }
                        TextButton(onClick = { dialog = TemplateDialog.DeleteBox(box) }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (state.templates.isEmpty()) {
                Text(
                    "Nothing here yet. Tap + to add a template.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 24.dp),
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.templates, key = { it.id }) { t ->
                    TemplateCard(
                        template = t,
                        boxName = boxNames[t.boxId] ?: "",
                        onFavorite = { vm.toggleFavorite(t) },
                        onEdit = { dialog = TemplateDialog.Editor(t) },
                        onDuplicate = { vm.duplicate(t) },
                        onDelete = { dialog = TemplateDialog.DeleteTemplate(t) },
                    )
                }
            }
        }
    }

    when (val d = dialog) {
        TemplateDialog.None -> Unit
        TemplateDialog.NewBox -> NameDialog(
            title = "New box",
            initial = "",
            confirmLabel = "Create",
            onDismiss = { dialog = TemplateDialog.None },
        ) { vm.addBox(it); dialog = TemplateDialog.None }
        is TemplateDialog.RenameBox -> NameDialog(
            title = "Rename box",
            initial = d.box.name,
            confirmLabel = "Save",
            onDismiss = { dialog = TemplateDialog.None },
        ) { vm.renameBox(d.box, it); dialog = TemplateDialog.None }
        is TemplateDialog.DeleteBox -> ConfirmDialog(
            title = "Delete box?",
            message = "\"${d.box.name}\" and every template inside it will be deleted.",
            onDismiss = { dialog = TemplateDialog.None },
        ) { vm.deleteBox(d.box); dialog = TemplateDialog.None }
        is TemplateDialog.DeleteTemplate -> ConfirmDialog(
            title = "Delete template?",
            message = "\"${d.template.title}\" will be deleted.",
            onDismiss = { dialog = TemplateDialog.None },
        ) { vm.delete(d.template); dialog = TemplateDialog.None }
        is TemplateDialog.Editor -> EditorDialog(
            initial = d.template,
            boxes = state.boxes,
            defaultBoxId = (state.filter as? TemplateFilter.Box)?.id,
            onDismiss = { dialog = TemplateDialog.None },
        ) { vm.saveTemplate(it); dialog = TemplateDialog.None }
    }
}

@Composable
private fun TemplateCard(
    template: TemplateEntity,
    boxName: String,
    onFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    Card(
        Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    template.title,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onFavorite) {
                    Icon(
                        if (template.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (template.favorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = { menu = false; onDuplicate() })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            Text(
                template.content,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "$boxName · ${TextStats.characters(template.content)} chars · ${TextStats.words(template.content)} words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NameDialog(
    title: String,
    initial: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(TemplatesViewModel.MAX_BOX_NAME) },
                singleLine = true,
                label = { Text("Name") },
            )
        },
        confirmButton = {
            TextButton(enabled = name.isNotBlank(), onClick = { onConfirm(name) }) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditorDialog(
    initial: TemplateEntity?,
    boxes: List<BoxEntity>,
    defaultBoxId: Long?,
    onDismiss: () -> Unit,
    onSave: (TemplateEntity) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var boxId by remember { mutableStateOf<Long?>(initial?.boxId ?: defaultBoxId ?: boxes.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New template" else "Edit template") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(TemplatesViewModel.MAX_TITLE) },
                    label = { Text("Title (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it.take(TemplatesViewModel.MAX_CONTENT) },
                    label = { Text("Text (emoji & multiline OK)") },
                    minLines = 4,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    "${TextStats.characters(content)} characters · ${TextStats.words(content)} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Box", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(boxes, key = { it.id }) { b ->
                        FilterChip(
                            selected = boxId == b.id,
                            onClick = { boxId = b.id },
                            label = { Text(b.name) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = content.isNotBlank() && boxId != null,
                onClick = {
                    val chosenBox = boxId ?: return@TextButton
                    val base = initial ?: TemplateEntity(boxId = chosenBox, title = "", content = "")
                    val finalTitle = title.trim().ifBlank { content.trim().lines().first().take(40) }
                    onSave(base.copy(boxId = chosenBox, title = finalTitle, content = content))
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
