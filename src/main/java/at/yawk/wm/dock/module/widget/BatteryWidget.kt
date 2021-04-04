package at.yawk.wm.dock.module.widget

import at.yawk.wm.dbus.Power
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.ui.*
import at.yawk.wm.x.icon.LoadedIcon
import at.yawk.wm.x.icon.IconManager
import java.time.Duration
import java.util.*
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.math.abs

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT, priority = -100)
class BatteryWidget @Inject constructor(
        val fontSource: FontSource,
        val iconManager: IconManager,
        val power: Power
) : FlowCompositeWidget() {
    private val devices = ArrayList<DeviceHolder>()

    private fun findChargeIcon(charging: Boolean, charge: Float): LoadedIcon {
        val iconSet = if (charging) DockConfig.chargingIcons else DockConfig.dischargingIcons
        val descriptor = iconSet.minByOrNull { e -> abs(e.key - charge) }!!.value
        return iconManager.getIcon(descriptor)
    }

    @Inject
    internal fun listen(executor: Executor, renderElf: RenderElf) {
        power.onPropertiesChanged {
            executor.execute {
                updateBattery()
                renderElf.render()
            }
        }
    }

    private val batteries: List<BatteryState>
        get() {
            if (!power.isPresent) {
                return emptyList()
            }

            val charge = (power.percentage / 100).toFloat()
            val charging: Boolean
            when (power.state) {
                1, 4 -> charging = true
                else -> charging = false
            }
            val remaining = Duration.ofSeconds(if (charging) power.timeToFull else power.timeToEmpty)

            return listOf(BatteryState(charge = charge, isCharging = charging, remaining = remaining))
        }

    @Periodic(30)
    @Synchronized internal fun updateBattery() {
        val batteries = batteries

        var deviceIterator: MutableIterator<DeviceHolder>? = devices.iterator()
        for (battery in batteries) {
            val holder: DeviceHolder
            if (deviceIterator != null && deviceIterator.hasNext()) {
                holder = deviceIterator.next()
            } else {
                holder = DeviceHolder()
                devices.add(holder)
                deviceIterator = null
            }
            holder.updateState(battery)
        }

        while (deviceIterator != null && deviceIterator.hasNext()) {
            deviceIterator.next().free()
            deviceIterator.remove()
        }
    }

    private data class BatteryState(
            val charge: Float,
            val isCharging: Boolean,
            val remaining: Duration
    )

    private inner class DeviceHolder {
        internal val percentage: TextWidget
        internal val duration: TextWidget
        internal val icon: IconWidget

        init {
            duration = TextWidget()
            duration.font = fontSource.getFont(DockConfig.batteryTime)
            val anchor = if (devices.isEmpty())
                anchor
            else
                devices[devices.size - 1].icon
            duration.after(anchor, Direction.HORIZONTAL)
            duration.textHeight = DockConfig.height
            duration.paddingLeft = 0

            percentage = TextWidget()
            percentage.after(duration, Direction.HORIZONTAL)
            percentage.textHeight = DockConfig.height
            percentage.paddingLeft = 0

            icon = IconWidget()
            icon.setColor(DockConfig.batteryTime)
            icon.after(percentage, Direction.HORIZONTAL)
            icon.targetHeight = DockConfig.height

            addWidget(duration)
            addWidget(percentage)
            addWidget(icon)
        }

        internal fun updateState(state: BatteryState) {
            percentage.text = Math.round(state.charge * 100).toString() + "%"
            val transition = DockConfig.batteryTransition.computeStyle(state.charge)
            percentage.font = fontSource.getFont(transition)

            val durationStr = StringBuilder()
            var seconds = state.remaining.getSeconds()
            if (seconds >= 60 * 60) {
                durationStr.append(seconds / (60 * 60)).append('h')
                seconds %= (60 * 60).toLong()
            }
            if (seconds >= 60) {
                durationStr.append(seconds / 60).append('m')
            }
            duration.text = durationStr.toString()

            icon.icon = findChargeIcon(state.isCharging, state.charge)
        }

        internal fun free() {
            removeWidget(duration)
            removeWidget(percentage)
            removeWidget(icon)
        }
    }
}
