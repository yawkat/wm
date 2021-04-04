package at.yawk.wm.tac

import at.yawk.wm.style.Color
import at.yawk.wm.style.StyleConfig

/**
 * @author yawkat
 */
object TacConfig {
    val fontPrimary = StyleConfig.tacPrimary
    val fontPrimarySelected = StyleConfig.tacPrimarySelected
    val fontSecondary = StyleConfig.tacSecondary
    val fontSecondarySelected = StyleConfig.tacSecondarySelected

    val colorBackground = Color.Solarized.base03
    val colorSelected = Color.Solarized.base02

    const val width = 400
    const val rowHeight = 22
    const val padding = 3
}
