package at.yawk.wm.x.font

import at.yawk.wm.style.FontStyle

interface GlyphFileFactory {
    fun renderRange(
        cellWidth: Byte,
        cellHeight: Byte,
        style: FontStyle,
        startInclusive: Char,
        endInclusive: Char
    ): GlyphFile
}