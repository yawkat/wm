package at.yawk.wm.dock.module.widget

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.Util.split
import at.yawk.wm.dock.module.DockConfig.cpuIcon
import at.yawk.wm.dock.module.DockConfig.cpuTransition
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.ui.TextWidget
import at.yawk.wm.x.icon.IconManager
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

@PerMonitor
@DockWidget(position = DockWidget.Position.RIGHT, priority = 100)
class CpuWidget @Inject constructor(
    private val fontSource: FontSource,
    private val iconManager: IconManager
) : TextWidget() {
    private val cpuUsage = MovingAverage(0.8)
    private var lastTime: Long = 0
    private var lastShares: Long = 0

    init {
        updateTextValue()
    }

    override fun init() {
        icon = iconManager.getIconOrNull(cpuIcon)
    }

    @Periodic(value = 1, render = true)
    @Throws(IOException::class)
    fun update() {
        var cpuCount = 0
        lateinit var firstLine: String
        Files.newBufferedReader(STAT_PATH).use { reader ->
            firstLine = reader.readLine()
            while (reader.readLine().startsWith("cpu")) {
                cpuCount++
            }
        }
        val items = split(firstLine, ' ', 4)
        val shares = java.lang.Long.parseUnsignedLong(items[1]) +
                java.lang.Long.parseUnsignedLong(items[2]) +
                java.lang.Long.parseUnsignedLong(items[3])
        val now = System.currentTimeMillis()
        if (lastTime != 0L) {
            var timeDelta = now - lastTime
            if (timeDelta <= 0) {
                timeDelta = 1
            } // prevent NaN
            val shareDelta = shares - lastShares
            // shares are emitted at 100Hz, time at 1000Hz (1000ms/s) so we need to do *10
            val usage = shareDelta * 10.0 / timeDelta / cpuCount
            cpuUsage.offer(usage)
        }
        lastShares = shares
        lastTime = now

        // one significant digit precision
        updateTextValue()
    }

    private fun updateTextValue() {
        text = formatPercent(cpuUsage.average)
        font = fontSource.getFont(cpuTransition.computeStyle(cpuUsage.average.toFloat()))
    }

    companion object {
        private val STAT_PATH = Paths.get("/proc/stat")

        @JvmStatic
        fun formatPercent(number: Double): String {
            return String.format("%04.1f%%", number * 100)
        }
    }
}