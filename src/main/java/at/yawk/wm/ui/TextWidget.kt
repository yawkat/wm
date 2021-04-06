package at.yawk.wm.ui

import at.yawk.wm.x.Graphics
import at.yawk.wm.x.font.GlyphFont
import at.yawk.wm.x.icon.LoadedIcon
import java.awt.Dimension

/**
 * @author yawkat
 */
open class TextWidget @JvmOverloads constructor(text: String = "") : Widget() {
    var text: String = text
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var icon: LoadedIcon? = null
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var font: GlyphFont? = null
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var textHeight = 0
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var paddingLeft = 4
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var paddingRight = 4
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }

    private lateinit var layoutTextBounds: Dimension
    private var boxWidth = 0
    private var boxHeight = 0

    override fun layout(graphics: Graphics) {
        layoutTextBounds = font!!.getStringBounds(text)
        boxWidth = layoutTextBounds.width
        if (icon != null) {
            boxWidth += icon!!.width
        }
        boxHeight = if (textHeight == 0) {
            layoutTextBounds.getHeight().toInt()
        } else {
            textHeight
        }
        width = boxWidth + paddingLeft + paddingRight
        height = boxHeight
    }

    override fun render(graphics: Graphics) {
        // local copy
        val text = text
        val x0 = Math.min(x, x2)
        val x = x0 + paddingLeft
        var y = Math.min(y, y2)
        if (textHeight != 0) {
            // center text vertically
            y += (textHeight - layoutTextBounds.height) / 2
        }
        if (paddingLeft > 0 || paddingRight > 0) {
            graphics.setForegroundColor(font!!.style.background.awt)
            graphics.fillRect(x0, y, boxWidth + paddingLeft + paddingRight, boxHeight)
        }
        var textStartX = x
        if (icon != null) {
            graphics.drawPixMap(icon!!.colorize(font!!.style.foreground.awt, font!!.style.background.awt), x, y)
            textStartX += icon!!.width
        }
        graphics.setFont(font!!)
        graphics.drawText(textStartX, y, text!!)
    }
}