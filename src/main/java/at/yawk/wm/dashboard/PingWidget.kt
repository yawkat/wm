package at.yawk.wm.dashboard

import at.yawk.wm.Scheduler
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.*
import java.io.IOException
import java.net.InetAddress
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author yawkat
 */
class PingWidget @Inject constructor(
        val fontSource: FontSource,
        val dashboardConfig: DashboardConfig,
        val pingManager: PingManager,
        val renderElf: RenderElf
) : FlowCompositeWidget() {
    private val destinationWidgets = HashMap<PingDestination, TextWidget>()

    init {
        var last: Positioned = anchor
        for (destination in dashboardConfig.pingDestinations.map { PingDestination(it.key, it.value) }) {
            val widget = TextWidget()
            widget.font = fontSource.getFont(dashboardConfig.pingFont)
            widget.after(last, Direction.VERTICAL)
            addWidget(widget)
            destinationWidgets[destination] = widget

            last = widget
        }

        pingManager.subscribers += { update() }
        update()
    }

    fun update() {
        synchronized(this) {
            destinationWidgets.forEach { destination, widget ->
                val ping = pingManager.pings[destination.host]
                widget.text = destination.name + ": " + (if (ping != null) "$ping ms" else "Timeout")
            }
        }
        renderElf.render()
    }
}

@Singleton
class PingManager @Inject constructor(val dashboardConfig: DashboardConfig, val scheduler: Scheduler) {
    var pings = emptyMap<String, Long?>()

    internal var subscribers = emptyList<() -> Unit>()

    fun start() {
        scheduler.scheduleAtFixedRate(
                Runnable { ping() },
                0, 10, TimeUnit.SECONDS
        )
    }

    private fun ping() {
        val newPings = HashMap<String, Long?>()
        for (destination in dashboardConfig.pingDestinations.values) {
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
        pings = newPings
        subscribers.forEach { it() }
    }
}

private data class PingDestination(
        val name: String,
        val host: String
)