package at.yawk.wm.dock.module

import at.yawk.wm.style.FontDescriptor
import at.yawk.wm.style.FontTransition
import at.yawk.wm.x.icon.IconDescriptor
import java.awt.Color

/**
 * @author yawkat
 */
data class DockConfig(
        val background: Color,

        val time: FontDescriptor,

        val batteryTime: FontDescriptor,
        val batteryTransition: FontTransition,
        val chargingIcons: Map<Float, IconDescriptor>,
        val dischargingIcons: Map<Float, IconDescriptor>,

        val cpuTransition: FontTransition,
        val cpuIcon: IconDescriptor,
        val memoryTransition: FontTransition,
        val swapTransition: FontTransition,

        val clockFormat: String,
        val clockFont: FontDescriptor,

        val netUpFont: FontDescriptor,
        val netDownFont: FontDescriptor,
        val netIconFont: FontDescriptor,
        val netIconOnline: IconDescriptor,
        val netIconOffline: IconDescriptor,

        val activeFont: FontDescriptor,
        val runningFont: FontDescriptor,
        val emptyFont: FontDescriptor,

        val windowTitleFont: FontDescriptor,

        val progressColor: Color,

        val mediaFont: FontDescriptor,
        val mediaPlaying: IconDescriptor,
        val mediaPaused: IconDescriptor,

        val height: Int
)
