package com.quicktext.panel.util

object TextStats {
    /** Counts Unicode code points, so an emoji counts as one character. */
    fun characters(text: String): Int = text.codePointCount(0, text.length)

    fun words(text: String): Int = text.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
}
