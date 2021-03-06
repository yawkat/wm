package at.yawk.wm.dock.module.widget

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.TimedCache
import at.yawk.wm.Util
import at.yawk.wm.dbus.NetworkManager
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.IconWidget
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.ui.TextWidget
import at.yawk.wm.x.icon.IconManager
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val showOnline = false

@PerMonitor
@DockWidget(position = DockWidget.Position.RIGHT, priority = 50)
class NetworkWidget @Inject internal constructor(
    val iconManager: IconManager,
    private val networkManager: NetworkManager,
    val renderElf: RenderElf,
    private val cacheHolder: CacheHolder,
    periodBuilder: PeriodBuilder
) : FlowCompositeWidget() {
    private val down = TextWidget()
    private val iconWidget: IconWidget = IconWidget()
    private val up: TextWidget = TextWidget()

    init {
        periodBuilder.submit(::update, TimeUnit.SECONDS.toMillis(1).toInt(), render = true)
        if (showOnline) {
            periodBuilder.submit(::updateOnline, TimeUnit.SECONDS.toMillis(30).toInt(), render = true)
        }
    }

    @Inject
    internal fun init(executor: Executor, fontSource: FontSource) {
        up.font = fontSource.getFont(DockConfig.netUpFont)
        up.after(anchor, Direction.HORIZONTAL)
        addWidget(up)

        down.font = fontSource.getFont(DockConfig.netDownFont)
        down.after(up, Direction.HORIZONTAL)
        addWidget(down)

        down.paddingLeft = 2
        up.paddingLeft = 0

        if (showOnline) {
            iconWidget.setColor(DockConfig.netIconFont)
            iconWidget.after(down, Direction.HORIZONTAL)
            addWidget(iconWidget)

            networkManager.onStateChanged {
                executor.execute {
                    updateOnline()
                    renderElf.render()
                }
            }
        }
    }

    private fun update() {
        val (downAverage, upAverage) = cacheHolder.cache.get()
        down.text = format(downAverage.average)
        up.text = format(upAverage.average)
    }

    private fun updateOnline() {
        val online = networkManager.connectivity > 1
        iconWidget.icon = iconManager.getIconOrNull(if (online) DockConfig.netIconOnline else DockConfig.netIconOffline)
    }

    @Singleton
    internal class CacheHolder @Inject constructor() {
        val cache = TimedCache<Pair<MovingAverage, MovingAverage>>(500, TimeUnit.MILLISECONDS) { old ->
            val process = ProcessBuilder()
                    .command("nstat", "-t", "1")
                    .start()

            var downOctets: Long = 0
            var upOctets: Long = 0
            process.inputStream.bufferedReader().use { reader ->
                reader.forEachLine { line ->
                    if (line.startsWith("IpExtInOctets")) {
                        downOctets = java.lang.Long.parseUnsignedLong(Util.split(line, ' ', 2)[1])
                    } else if (line.startsWith("IpExtOutOctets")) {
                        upOctets = java.lang.Long.parseUnsignedLong(Util.split(line, ' ', 2)[1])
                    }
                }
            }

            val averages = old ?: Pair(MovingAverage(0.8), MovingAverage(0.8))

            val (downAverage, upAverage) = averages
            downAverage.offer(downOctets.toDouble())
            upAverage.offer(upOctets.toDouble())

            averages
        }
    }

    private fun format(traffic: Double): String {
        var traffic = traffic
        var exp = 0
        while (traffic >= 1000) {
            traffic /= 1024.0
            exp++
        }
        val unit: Char = when (exp) {
            0 -> 'B'
            1 -> 'K'
            2 -> 'M'
            3 -> 'G'
            4 -> 'T'
            else -> '?'
        }
        return String.format("%05.1f%s", traffic, unit)
    }
}
