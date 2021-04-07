package at.yawk.wm.x.font

@Suppress("ArrayInDataClass")
data class GlyphFile(
    val cellWidth: Byte,
    val cellHeight: Byte,
    val startInclusive: Char,
    val endInclusive: Char,
    val data: ByteArray
) {
    /*
     * Glyph file format:
     *
     * 1. HEADER
     *  for each glyph, there are 4 bytes of header data:
     *   - glyph width
     *   - glyph height
     *   - glyph ascent
     *   - glyph median
     * 2. BODY
     *  following is the glyph line. each glyph takes up a fixed-size cell in which
     *  it is placed at the top left (x = y = 0). the ascender height is at y = 0, the baseline
     *  at y = ascent (from header). the image is a simple byte array with the value of each
     *  pixel taking up exactly three bytes (r-g-b in order). it is saved line-by-line.
     *
     * if the glyph width is larger than the cell width, the character is clipped. the renderer
     * should assume the missing space to be transparent.
     */

    val charCount: Int
        get() = endInclusive - startInclusive + 1

    private fun getMeta(c: Char, headerIndex: Int): Byte {
        val i = (c - startInclusive) * GLYPH_HEADER_LENGTH + headerIndex
        return data[i]
    }

    fun getWidth(c: Char): Int {
        return getMeta(c, 0).toInt()
    }

    fun getHeight(c: Char): Int {
        return getMeta(c, 1).toInt()
    }

    fun getAscent(c: Char): Int {
        return getMeta(c, 2).toInt()
    }

    /**
     * Free glyph pixels. Metadata will stay available.
     */
    fun discardBody() = copy(data = data.copyOf(GLYPH_HEADER_LENGTH * charCount))

    companion object {
        const val GLYPH_HEADER_LENGTH = 3
    }
}