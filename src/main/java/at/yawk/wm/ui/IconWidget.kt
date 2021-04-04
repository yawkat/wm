package at.yawk.wm.ui

import at.yawk.wm.style.FontStyle
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.icon.LoadedIcon
import java.awt.Color
import kotlin.math.min

class IconWidget : Widget() {
    var icon: LoadedIcon? = null
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var foreground: Color? = null
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var background: Color? = null
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }
    var targetHeight = -1
        set(value) {
            if (field != value) {
                field = value
                markDirty()
            }
        }

    fun setColor(style: FontStyle) {
        this.foreground = style.foreground.awt
        this.background = style.background.awt
    }

    override fun layout(graphics: Graphics) {
        width = if (icon != null) icon!!.width else 0
        height = if (targetHeight != -1) {
            targetHeight
        } else {
            if (icon != null) icon!!.height else 0
        }
    }

    override fun render(graphics: Graphics) {
        if (icon != null) {
            val x = min(x, x2)
            var y = min(y, y2)
            if (targetHeight != -1) {
                // center vertically
                y += (targetHeight - icon!!.height) / 2
            }
            graphics.drawPixMap(icon!!.colorize(foreground, background), x, y)
        }
    }
}