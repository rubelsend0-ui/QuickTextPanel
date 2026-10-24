package com.quicktext.panel.ui.templates

import com.quicktext.panel.data.local.TemplateEntity

sealed interface TemplateFilter {
    data object All : TemplateFilter
    data object Favorites : TemplateFilter
    data class Box(val id: Long) : TemplateFilter
}

fun applyFilter(items: List<TemplateEntity>, filter: TemplateFilter, query: String): List<TemplateEntity> {
    val q = query.trim().lowercase()
    return items.filter { t ->
        val inFilter = when (filter) {
            TemplateFilter.All -> true
            TemplateFilter.Favorites -> t.favorite
            is TemplateFilter.Box -> t.boxId == filter.id
        }
        inFilter && (q.isEmpty() || t.title.lowercase().contains(q) || t.content.lowercase().contains(q))
    }
}
