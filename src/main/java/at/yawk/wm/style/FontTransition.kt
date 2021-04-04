package at.yawk.wm.style

import kotlin.math.roundToInt

/**
 * @author yawkat
 */
data class FontTransition(val low: FontStyle, val high: FontStyle) {
    fun computeStyle(value: Float): FontStyle {
        return low.copy(foreground = shade(low.foreground, high.foreground, value))
    }
}

private const val PRECISION = 20

private fun shade(low: Color, high: Color, value: Float): Color {
    val m2 = (value * PRECISION).roundToInt().toFloat() / PRECISION
    if (m2 <= 0) {
        return low
    }
    if (m2 >= 1) {
        return high
    }
    val hsbLow = toHSB(low)
    val hsbHigh = toHSB(high)
    val m1 = 1 - m2
    val rgb: Int = java.awt.Color.HSBtoRGB(
        hsbLow[0] * m1 + hsbHigh[0] * m2,
        hsbLow[1] * m1 + hsbHigh[1] * m2,
        hsbLow[2] * m1 + hsbHigh[2] * m2
    )
    return Color(rgb)
}

private fun toHSB(color: Color): FloatArray {
    return java.awt.Color.RGBtoHSB(color.red, color.green, color.blue, null)
}