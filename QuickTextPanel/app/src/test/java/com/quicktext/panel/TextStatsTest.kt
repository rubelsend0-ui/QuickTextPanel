package com.quicktext.panel

import com.quicktext.panel.util.TextStats
import org.junit.Assert.assertEquals
import org.junit.Test

class TextStatsTest {
    @Test fun emptyTextHasNoWords() {
        assertEquals(0, TextStats.words(""))
        assertEquals(0, TextStats.words("   \n  "))
        assertEquals(0, TextStats.characters(""))
    }

    @Test fun countsWordsAcrossLines() {
        assertEquals(4, TextStats.words("Hello  world\nfrom   QuickText"))
    }

    @Test fun emojiCountsAsOneCharacter() {
        assertEquals(1, TextStats.characters("\uD83D\uDE00"))
        assertEquals(3, TextStats.characters("a\uD83D\uDE00b"))
    }

    @Test fun unicodeTextIsCounted() {
        assertEquals(2, TextStats.words("नमस्ते दुनिया"))
    }
}
