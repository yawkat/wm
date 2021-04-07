package at.yawk.wm.dashboard

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.TimedCache
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.Positioned
import at.yawk.wm.ui.TextWidget
import java.io.IOException
import java.net.InetAddress
import java.util.HashMap
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@PerMonitor
class PingWidget @Inject internal constructor(
    fontSource: FontSource,
    private val cacheHolder: CacheHolder,
    periodBuilder: PeriodBuilder
) : FlowCompositeWidget() {
    private val destinationWidgets = HashMap<PingDestination, TextWidget>()

    init {
        var last: Positioned = anchor
        for (destination in DashboardConfig.pingDestinations.map { PingDestination(it.key, it.value) }) {
            val widget = TextWidget()
            widget.font = fontSource.getFont(DashboardConfig.pingFont)
            widget.after(last, Direction.VERTICAL)
            addWidget(widget)
            destinationWidgets[destination] = widget

            last = widget
        }

        periodBuilder.submit(::update, TimeUnit.SECONDS.toMillis(10).toInt(), render = true)
    }

    private fun update() {
        val pings = cacheHolder.cache.get()
        destinationWidgets.forEach { (destination, widget) ->
            val ping = pings[destination.host]
            widget.text = destination.name + ": " + (if (ping != null) "$ping ms" else "Timeout")
        }
    }

    @Singleton
    internal class CacheHolder @Inject constructor() {
        val cache = TimedCache<Map<String, Long?>>(9, TimeUnit.SECONDS) {
            val newPings = HashMap<String, Long?>()
            for (destination in DashboardConfig.pingDestinations.values) {
                if (!newPings.containsKey(destination)) {
                    try {
                        val start = System.currentTimeMillis()
                        val reachable = InetAddress.getByName(destination).isReachable(1000)
                        val end = System.currentTimeMillis()
                        newPings[destination] = if (reachable) end - start else null
                    } catch (io: IOException) {
                        // ignore io errors
                    }
                }
            }

            newPings
        }
    }
}

private data class PingDestination(
        val name: String,
        val host: String
)