package at.yawk.wm.dashboard

import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.Monitor
import at.yawk.wm.x.EventGroup
import at.yawk.wm.x.WindowType
import at.yawk.wm.x.XcbConnector
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DesktopManager @Inject constructor(
        val herbstClient: HerbstClient,
        val xcbConnector: XcbConnector
) {
    private val desktops = HashMap<Int, Desktop>()

    fun init() {
        for (monitor in herbstClient.listMonitors()) {
            desktops[monitor.id] = Desktop(monitor)
        }
    }

    fun getDesktop(monitor: Monitor): Desktop {
        return desktops[monitor.id] ?: throw IllegalArgumentException("Unknown monitor $monitor")
    }

    fun getDesktops(): Collection<Desktop> = desktops.values

    inner class Desktop internal constructor(val monitor: Monitor) {
        val window = xcbConnector.screen.createWindow(EventGroup.PAINT)

        init {
            window.setBounds(monitor.x, monitor.y, monitor.width, monitor.height)
            window.setType(WindowType.DESKTOP)
            window.show()
        }
    }
}
