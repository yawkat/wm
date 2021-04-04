package at.yawk.wm.dock.module

import at.yawk.wm.style.Color
import at.yawk.wm.style.FontTransition
import at.yawk.wm.style.Icon
import at.yawk.wm.style.StyleConfig
import java.nio.file.Path
import java.time.format.DateTimeFormatter

/**
 * @author yawkat
 */
object DockConfig {
    val background = Color.Solarized.base03

    val time = StyleConfig.base0

    val batteryTime = StyleConfig.base0
    val batteryTransition = FontTransition(low = StyleConfig.red, high = StyleConfig.green)
    val chargingIcons = mapOf<Float, Icon>(
        0.0F to Icon.battery_0,
        0.2F to Icon.battery_20,
        0.3F to Icon.battery_30,
        0.5F to Icon.battery_50,
        0.6F to Icon.battery_60,
        0.8F to Icon.battery_80,
        0.9F to Icon.battery_90,
        1.0F to Icon.battery_100,
    )
    val dischargingIcons = mapOf<Float, Icon>(
        0.2F to Icon.battery_20_charging,
        0.3F to Icon.battery_30_charging,
        0.5F to Icon.battery_50_charging,
        0.6F to Icon.battery_60_charging,
        0.8F to Icon.battery_80_charging,
        0.9F to Icon.battery_90_charging,
        1.0F to Icon.battery_100_charging,
    )

    val cpuTransition = FontTransition(low = StyleConfig.green, high = StyleConfig.red)
    val cpuIcon = Icon.cpu
    val memoryTransition = cpuTransition
    val swapTransition = cpuTransition

    val clockFormat = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss")!!
    val clockFont = StyleConfig.base0

    val netUpFont = StyleConfig.yellow
    val netDownFont = StyleConfig.red
    val netIconFont = StyleConfig.base0
    val netIconOnline = Icon.net_online
    val netIconOffline = Icon.net_offline

    val activeFont = StyleConfig.highlight
    val activeElsewhereFont = StyleConfig.highlightLow
    val runningFont = StyleConfig.base0
    val emptyFont = StyleConfig.base01

    val windowTitleFont = StyleConfig.base01

    val progressColor = Color.Solarized.base1

    val activeMonitorColor = Color(0x59621b)

    val mediaFont = StyleConfig.base0

    const val height = 20
}
