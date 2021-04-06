package at.yawk.wm.dock.module

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.feature.DockPadFeature
import at.yawk.wm.dock.module.feature.ScrollTagChange
import at.yawk.wm.dock.module.widget.BatteryWidget
import at.yawk.wm.dock.module.widget.ClockWidget
import at.yawk.wm.dock.module.widget.CpuWidget
import at.yawk.wm.dock.module.widget.MediaWidget
import at.yawk.wm.dock.module.widget.MemoryWidget
import at.yawk.wm.dock.module.widget.NetworkWidget
import at.yawk.wm.dock.module.widget.ProgressWidget
import at.yawk.wm.dock.module.widget.TagListWidget
import at.yawk.wm.dock.module.widget.TitleWidget
import at.yawk.wm.ui.Widget
import javax.inject.Inject

@PerMonitor
class DockBootstrap @Inject constructor(
    val dock: DockBuilder,
    val periodBuilder: PeriodBuilder,

    batteryWidget: BatteryWidget,
    clockWidget: ClockWidget,
    cpuWidget: CpuWidget,
    mediaWidget: MediaWidget,
    memoryWidget: MemoryWidget,
    networkWidget: NetworkWidget,
    progressWidget: ProgressWidget,
    tagListWidget: TagListWidget,
    titleWidget: TitleWidget,
    private val dockPadFeature: DockPadFeature,
    private val scrollTagChange: ScrollTagChange
) {
    val widgets: List<Widget> = listOf(
        batteryWidget,
        clockWidget,
        cpuWidget,
        mediaWidget,
        memoryWidget,
        networkWidget,
        progressWidget,
        tagListWidget,
        titleWidget
    )

    val dockStartListeners = listOf(Runnable { dockPadFeature.init() }, Runnable { scrollTagChange.listen() })

    fun startDock() {
        dock.start(this)
    }
}