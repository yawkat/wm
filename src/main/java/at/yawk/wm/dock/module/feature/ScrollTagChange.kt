package at.yawk.wm.dock.module.feature

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockBuilder
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.Monitor
import at.yawk.wm.x.event.Button
import at.yawk.wm.x.event.ButtonPressEvent
import javax.inject.Inject

@PerMonitor
class ScrollTagChange @Inject constructor(
    private val dockBuilder: DockBuilder,
    private val herbstClient: HerbstClient,
    private val monitor: Monitor
) {
    fun listen() {
        dockBuilder.window.addListener(ButtonPressEvent::class.java) { evt: ButtonPressEvent ->
            herbstClient.focusMonitor(monitor)
            if (evt.contains(Button.SCROLL_UP)) {
                herbstClient.advanceTag(-1)
            }
            if (evt.contains(Button.SCROLL_DOWN)) {
                herbstClient.advanceTag(+1)
            }
        }
    }
}