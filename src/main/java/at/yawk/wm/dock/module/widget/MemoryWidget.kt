package at.yawk.wm.dock.module.widget

import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.dock.module.widget.CpuWidget.Companion.formatPercent
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.TextWidget
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.inject.Inject

/**
 * @author yawkat
 */
@DockWidget(position = DockWidget.Position.RIGHT, priority = 99)
class MemoryWidget @Inject constructor(private val fontSource: FontSource) : FlowCompositeWidget() {
    private val ram: TextWidget
    private val swap: TextWidget

    @Periodic(value = 1, render = true)
    @Throws(IOException::class)
    fun update() {
        var memTotal: Long = 0
        var memFree: Long = 0
        var buffers: Long = 0
        var cached: Long = 0
        var swapTotal: Long = 0
        var swapFree: Long = 0
        Files.newBufferedReader(MEMINFO_PATH).use { reader ->
            reader.forEachLine { line ->
                if (line.startsWith("MemTotal:")) {
                    memTotal = parseMem(line, 10)
                } else if (line.startsWith("MemFree:")) {
                    memFree = parseMem(line, 9)
                } else if (line.startsWith("Cached:")) {
                    cached = parseMem(line, 8)
                } else if (line.startsWith("Buffers:")) {
                    buffers = parseMem(line, 9)
                } else if (line.startsWith("SwapTotal:")) {
                    swapTotal = parseMem(line, 11)
                } else if (line.startsWith("SwapFree:")) {
                    swapFree = parseMem(line, 10)
                }
            }
        }
        val ramUse: Float = if (memTotal == 0L) 1F else (memTotal - memFree - buffers - cached).toFloat() / memTotal
        val swapUse: Float = if (swapTotal == 0L) 1F else (swapTotal - swapFree).toFloat() / swapTotal
        ram.font = fontSource.getFont(DockConfig.memoryTransition.computeStyle(ramUse))
        ram.text = formatPercent(ramUse.toDouble())
        swap.font = fontSource.getFont(DockConfig.swapTransition.computeStyle(swapUse))
        swap.text = formatPercent(swapUse.toDouble())
        if (swapTotal == 0L) {
            swap.visibility = Visibility.GONE
        }
    }

    companion object {
        private val MEMINFO_PATH = Paths.get("/proc/meminfo")
        private fun parseMem(s: String, off: Int): Long {
            var amount: Long = 0
            for (i in off until s.length) {
                val c = s[i]
                if (c >= '0' && c <= '9') {
                    amount *= 10
                    amount += (c - '0').toLong()
                } else if (c == 'k') {
                    amount *= 1024
                } else if (c == 'm') {
                    amount *= (1024 * 1024).toLong()
                } else if (c == 'g') {
                    amount *= (1024 * 1024 * 1024).toLong()
                }
            }
            return amount
        }
    }

    init {
        swap = TextWidget()
        swap.after(anchor, Direction.HORIZONTAL)
        addWidget(swap)
        ram = TextWidget()
        ram.after(swap, Direction.HORIZONTAL)
        addWidget(ram)
    }
}