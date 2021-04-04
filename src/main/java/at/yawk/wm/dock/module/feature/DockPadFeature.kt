package at.yawk.wm.dock.module.feature

import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.dock.module.DockStart
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.hl.Monitor
import javax.inject.Inject

class DockPadFeature @Inject constructor(
    val herbstClient: HerbstClient,
    val monitor: Monitor
) {
    @DockStart
    fun init() {
        herbstClient.pad(monitor, DockConfig.height)
    }
}