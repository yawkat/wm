package at.yawk.wm.dashboard

import at.yawk.wm.style.FontDescriptor
import java.awt.Color

/**
 * @author yawkat
 */
data class DashboardConfig(
        val temperatureFont: FontDescriptor,
        val mediaFont: FontDescriptor,
        val pingFont: FontDescriptor,
        val xkcdFont: FontDescriptor,
        val xkcdWhite: Color,
        val xkcdBlack: Color,

        val pingDestinations: Map<String, String>
)