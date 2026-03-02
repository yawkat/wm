package at.yawk.wm.x.font

import at.yawk.wm.style.FontStyle
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File

object AwtGlyphFileFactory : GlyphFileFactory {
    private const val DEBUG_LINES = false

    fun getFont(style: FontStyle): Font {
        var flags = 0
        if (style.bold) {
            flags = flags or Font.BOLD
        }
        if (style.italic) {
            flags = flags or Font.ITALIC
        }
        return resolveRuntimeFont(style.family, flags, style.size)
            ?: Font(style.family, flags, style.size)
    }

    private val log = org.slf4j.LoggerFactory.getLogger(AwtGlyphFileFactory::class.java)

    /**
     * Resolve a font at runtime by loading the font file directly via fc-match.
     * This bypasses GraalVM native-image's broken font subsystem where Font constructor
     * bakes font mappings at build time, causing metrics to return 0.
     */
    private fun resolveRuntimeFont(family: String, flags: Int, size: Int): Font? {
        try {
            val process = ProcessBuilder("fc-match", family, "--format=%{file}")
                .redirectErrorStream(true)
                .start()
            val fontPath = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()

            if (fontPath.isBlank()) {
                log.warn("resolveRuntimeFont: fc-match returned empty for family='{}'", family)
                return null
            }

            val fontFile = File(fontPath)
            if (!fontFile.exists()) {
                log.warn("resolveRuntimeFont: font file does not exist: {}", fontPath)
                return null
            }

            val format = if (fontPath.endsWith(".otf", ignoreCase = true) || fontPath.endsWith(".ttf", ignoreCase = true)) {
                Font.TRUETYPE_FONT
            } else {
                Font.TYPE1_FONT
            }
            val baseFont = Font.createFont(format, fontFile)
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            ge.registerFont(baseFont)
            val derived = baseFont.deriveFont(flags, size.toFloat())
            log.info("resolveRuntimeFont: family='{}' -> file={} -> derived={}", family, fontPath, derived)

            return derived
        } catch (e: Exception) {
            log.warn("resolveRuntimeFont: failed for family='{}'", family, e)
            return null
        }
    }

    override fun renderRange(
        cellWidth: Byte,
        cellHeight: Byte,
        style: FontStyle,
        startInclusive: Char,
        endInclusive: Char
    ): GlyphFile {
        require(cellWidth > 0)
        require(cellHeight > 0)

        val font = getFont(style)

        val nChars = endInclusive - startInclusive + 1
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
        )
        val metrics = gfx.fontMetrics
        for (c in startInclusive..endInclusive) {
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
            data[headerPos + 1] = lineMetrics.height.toInt().toByte()
            data[headerPos + 2] = lineMetrics.ascent.toInt().toByte()
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
        return GlyphFile(cellWidth, cellHeight, startInclusive, endInclusive, data)
    }
}