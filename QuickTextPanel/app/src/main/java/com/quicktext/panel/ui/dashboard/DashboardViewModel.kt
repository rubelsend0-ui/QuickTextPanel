package com.quicktext.panel.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicktext.panel.data.local.TemplateEntity
import com.quicktext.panel.data.repository.TemplateRepository
import com.quicktext.panel.util.TextStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardState(
    val templates: Int = 0,
    val boxes: Int = 0,
    val favorites: Int = 0,
    val words: Int = 0,
    val characters: Int = 0,
    val recent: List<TemplateEntity> = emptyList(),
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TemplateRepository,
) : ViewModel() {
    val state: StateFlow<DashboardState> = combine(
        repository.observeBoxes(),
        repository.observeTemplates(),
    ) { boxes, templates ->
        DashboardState(
            templates = templates.size,
            boxes = boxes.size,
            favorites = templates.count { it.favorite },
            words = templates.sumOf { TextStats.words(it.content) },
            characters = templates.sumOf { TextStats.characters(it.content) },
            recent = templates.take(3),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
}
