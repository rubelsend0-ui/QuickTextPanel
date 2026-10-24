package com.quicktext.panel

import com.quicktext.panel.data.local.TemplateEntity
import com.quicktext.panel.ui.templates.TemplateFilter
import com.quicktext.panel.ui.templates.applyFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateFilterTest {
    private val items = listOf(
        TemplateEntity(id = 1, boxId = 1, title = "Greeting", content = "Hello there", favorite = true),
        TemplateEntity(id = 2, boxId = 1, title = "Address", content = "12 Main Street"),
        TemplateEntity(id = 3, boxId = 2, title = "Thanks", content = "Thank you \uD83D\uDE0A"),
    )

    @Test fun allWithoutQueryReturnsEverything() {
        assertEquals(3, applyFilter(items, TemplateFilter.All, "").size)
    }

    @Test fun favoritesOnly() {
        assertEquals(listOf(1L), applyFilter(items, TemplateFilter.Favorites, "").map { it.id })
    }

    @Test fun boxFilter() {
        assertEquals(listOf(3L), applyFilter(items, TemplateFilter.Box(2), "").map { it.id })
    }

    @Test fun searchIsCaseInsensitiveAndChecksContent() {
        assertEquals(listOf(2L), applyFilter(items, TemplateFilter.All, "MAIN").map { it.id })
        assertEquals(listOf(3L), applyFilter(items, TemplateFilter.All, "\uD83D\uDE0A").map { it.id })
    }
}
