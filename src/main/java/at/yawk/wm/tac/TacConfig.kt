package at.yawk.wm.tac

import at.yawk.wm.style.FontDescriptor
import java.awt.Color

/**
 * @author yawkat
 */
data class TacConfig(
        val fontPrimary: FontDescriptor,
        val fontPrimarySelected: FontDescriptor,
        val fontSecondary: FontDescriptor,
        val fontSecondarySelected: FontDescriptor,

        val colorBackground: Color,
        val colorSelected: Color,

        val width: Int,
        val rowHeight: Int,
        val padding: Int
)
