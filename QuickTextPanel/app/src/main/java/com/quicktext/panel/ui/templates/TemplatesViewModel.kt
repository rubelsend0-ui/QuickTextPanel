package com.quicktext.panel.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicktext.panel.data.local.BoxEntity
import com.quicktext.panel.data.local.TemplateEntity
import com.quicktext.panel.data.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TemplatesUiState(
    val boxes: List<BoxEntity> = emptyList(),
    val templates: List<TemplateEntity> = emptyList(),
    val filter: TemplateFilter = TemplateFilter.All,
    val query: String = "",
)

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val repository: TemplateRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow<TemplateFilter>(TemplateFilter.All)
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    val state: StateFlow<TemplatesUiState> = combine(
        repository.observeBoxes(),
        repository.observeTemplates(),
        filter,
        query,
    ) { boxes, all, f, q ->
        TemplatesUiState(boxes, applyFilter(all, f, q), f, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TemplatesUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(value: TemplateFilter) {
        filter.value = value
    }

    fun addBox(name: String) = launchSafe {
        if (name.isBlank()) return@launchSafe notify("Box name can't be empty")
        val id = repository.addBox(name.take(MAX_BOX_NAME))
        filter.value = TemplateFilter.Box(id)
    }

    fun renameBox(box: BoxEntity, name: String) = launchSafe {
        if (name.isBlank()) return@launchSafe notify("Box name can't be empty")
        repository.renameBox(box, name.take(MAX_BOX_NAME))
    }

    fun deleteBox(box: BoxEntity) = launchSafe {
        if (state.value.boxes.size <= 1) return@launchSafe notify("Keep at least one box")
        repository.deleteBox(box)
        if ((filter.value as? TemplateFilter.Box)?.id == box.id) filter.value = TemplateFilter.All
    }

    fun saveTemplate(template: TemplateEntity) = launchSafe {
        if (template.content.isBlank()) return@launchSafe notify("Template text can't be empty")
        repository.saveTemplate(template)
    }

    fun duplicate(template: TemplateEntity) = launchSafe { repository.duplicate(template) }
    fun delete(template: TemplateEntity) = launchSafe { repository.delete(template) }
    fun toggleFavorite(template: TemplateEntity) = launchSafe {
        repository.setFavorite(template.id, !template.favorite)
    }

    private fun notify(message: String) {
        _messages.tryEmit(message)
    }

    private fun launchSafe(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                notify("Something went wrong: ${e.localizedMessage ?: "unknown error"}")
            }
        }
    }

    companion object {
        const val MAX_TITLE = 80
        const val MAX_CONTENT = 10_000
        const val MAX_BOX_NAME = 40
    }
}
