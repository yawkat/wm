package at.yawk.wm.x

import at.yawk.wm.x.font.GlyphFont
import at.yawk.wm.x.image.LocalImage
import java.awt.*

/**
 * @author yawkat
 */
interface Graphics : Resource {
    fun setFont(font: String): Graphics

    fun setForegroundColor(color: Color): Graphics

    fun setBackgroundColor(color: Color): Graphics

    fun setFont(font: GlyphFont): Graphics

    fun drawText(x: Int, y: Int, text: String): Graphics

    fun fillRect(x: Int, y: Int, width: Int, height: Int): Graphics

    fun clearRect(x: Int, y: Int, width: Int, height: Int): Graphics

    fun drawPixMap(area: PixMapArea, x: Int, y: Int): Graphics

    fun drawPixMap(pixMap: PixMap, srcX: Int, srcY: Int, destX: Int, destY: Int, width: Int, height: Int): Graphics

    /**
     * Draw a local image onto this canvas.
     */
    fun putImage(x: Int, y: Int, image: LocalImage): Graphics

    fun flush()
}

inline fun <T> Graphics.use(function: (Graphics) -> T): T {
    try {
        return function(this)
    } finally {
        this.close()
    }
}
