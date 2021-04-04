package at.yawk.wm.x.font

import at.yawk.wm.style.FontStyle
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * @author yawkat
 */
internal class GlyphFileFactory(
    private val cellWidth: Byte,
    private val cellHeight: Byte,
    private val style: FontStyle
) {
    private val font: Font = style.family.createFont(style)

    fun renderRange(startInclusive: Char, endInclusive: Char): GlyphFile {
        val glyphFile = GlyphFile()
        glyphFile.startInclusive = startInclusive
        glyphFile.endInclusive = endInclusive
        glyphFile.cellWidth = cellWidth
        glyphFile.cellHeight = cellHeight
        val nChars = glyphFile.charCount
        val headerLen = nChars * GlyphFile.GLYPH_HEADER_LENGTH
        val data = ByteArray(
            headerLen +  // header
                    nChars * cellHeight * cellWidth * GlyphFile.GLYPH_HEADER_LENGTH // glyphs
        )
        val rowWidth = cellWidth * nChars
        val image = BufferedImage(
            rowWidth, cellHeight.toInt(), BufferedImage.TYPE_3BYTE_BGR
        )
        val gfx = image.createGraphics()
        gfx.font = font
        gfx.background = style.background.awt
        gfx.clearRect(0, 0, rowWidth, cellHeight.toInt())
        gfx.color = style.foreground.awt
        gfx.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        ) // todo config option
        val metrics = gfx.fontMetrics
        var c = startInclusive
        while (c <= endInclusive) {
            val s = c.toString()
            val lineMetrics = metrics.getLineMetrics(s, gfx)
            val x = (c - startInclusive) * cellWidth
            gfx.drawString(s, x.toFloat(), lineMetrics.ascent)
            val headerPos = (c - startInclusive) * GlyphFile.GLYPH_HEADER_LENGTH
            val width = metrics.charWidth(c)
            if (width > cellWidth) {
                gfx.clearRect(x + cellWidth, 0, width - cellWidth, cellHeight.toInt())
            }
            data[headerPos] = width.toByte()
            data[headerPos + 1] = lineMetrics.height.toByte()
            data[headerPos + 2] = lineMetrics.ascent.toByte()
            if (DEBUG_LINES) {
                gfx.color = Color.WHITE
                gfx.drawLine(x + cellWidth - 1, 0, x + cellWidth - 1, cellHeight.toInt())
                gfx.color = Color.RED
                gfx.drawLine(x + width, 0, x + width, cellHeight.toInt())
                val lh = lineMetrics.height.toInt()
                gfx.drawLine(x, lh, x + width, lh)
                gfx.color = Color.YELLOW
                gfx.drawLine(x, (lh - lineMetrics.ascent).toInt(), x + width, (lh - lineMetrics.ascent).toInt())

                // reset color
                gfx.color = style.foreground.awt
            }
            c++
        }
        gfx.dispose()
        val pixels = image.getRGB(0, 0, nChars * cellWidth, cellHeight.toInt(), null, 0, nChars * cellWidth)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val pos = headerLen + i * 3
            data[pos] = (pixel shr 16).toByte() // r
            data[pos + 1] = (pixel shr 8).toByte() // g
            data[pos + 2] = pixel.toByte() // b
        }
        glyphFile.data = data
        return glyphFile
    }

    companion object {
        private const val DEBUG_LINES = false
    }

}