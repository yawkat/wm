package at.yawk.wm.x.font

import at.yawk.wm.style.FontStyle
import java.awt.Dimension
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.max

private val glyphFileFactory: GlyphFileFactory = AwtGlyphFileFactory

class GlyphFont(
    val style: FontStyle,
    private val cacheRoot: Path? = null
) {
    private val cellHeight = (style.size * 1.5).toInt().toByte() // better too large than too small
    private val cellWidth = cellHeight

    private val glyphFiles = arrayOfNulls<GlyphFile>((Character.MAX_VALUE.toInt() + 1) / GLYPH_FILE_LENGTH)

    private val cacheFolder: Path?
        get() = cacheRoot?.resolve(style.getDescriptor() + "-" + cellWidth + "x" + cellHeight)

    fun resolveGlyphFile(c: Char): GlyphFile {
        val loaded = glyphFiles[c.toInt() / GLYPH_FILE_LENGTH]
        return loaded ?: loadGlyphFile(c)
    }

    @Synchronized
    private fun loadGlyphFile(c: Char): GlyphFile {
        var loaded = glyphFiles[c.toInt() / GLYPH_FILE_LENGTH]
        if (loaded != null) {
            return loaded
        }
        val startInclusive = (c.toInt() / GLYPH_FILE_LENGTH * GLYPH_FILE_LENGTH).toChar()
        val endInclusive = (startInclusive + GLYPH_FILE_LENGTH - 1)
        loaded = tryLoadCachedGlyphFile(startInclusive, endInclusive)
        if (loaded == null) {
            // need to generate
            loaded = glyphFileFactory.renderRange(cellWidth, cellHeight, style, startInclusive, endInclusive)
            val cacheFolder = cacheFolder
            if (cacheFolder != null) {
                try {
                    if (!Files.exists(cacheFolder)) {
                        Files.createDirectories(cacheFolder)
                    }
                    Files.write(getCacheFile(cacheFolder, startInclusive.toInt()), loaded.data)
                } catch (e: IOException) {
                    throw UncheckedIOException(e)
                }
            }
        }
        for (i in startInclusive.toInt() / GLYPH_FILE_LENGTH..endInclusive.toInt() / GLYPH_FILE_LENGTH) {
            glyphFiles[i] = loaded
        }
        return loaded
    }

    private fun tryLoadCachedGlyphFile(startInclusive: Char, endInclusive: Char): GlyphFile? {
        val cacheFolder = cacheFolder ?: return null
        val cacheFile = getCacheFile(cacheFolder, startInclusive.toInt())
        if (!Files.exists(cacheFile)) {
            return null
        }
        return GlyphFile(
            cellWidth = cellWidth,
            cellHeight = cellHeight,
            startInclusive = startInclusive,
            endInclusive = endInclusive,
            data = Files.readAllBytes(cacheFile)
        )
    }

    fun getStringBounds(s: CharSequence): Dimension {
        var width = 0
        var height = 0
        for (element in s) {
            val glyphFile = resolveGlyphFile(element)
            width += glyphFile.getWidth(element)
            height = max(height, glyphFile.getHeight(element))
        }
        return Dimension(width, height)
    }

    override fun toString(): String {
        return "GlyphFont(style=$style)"
    }

    companion object {
        private const val GLYPH_FILE_LENGTH = 256

        private fun getCacheFile(cacheFolder: Path, startInclusive: Int): Path {
            return cacheFolder.resolve(String.format("%04x", startInclusive))
        }
    }
}