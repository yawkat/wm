package at.yawk.wm.dock.module.widget

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockBuilder
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.Monitor
import at.yawk.wm.progress.ProgressManager
import at.yawk.wm.progress.ProgressTask
import at.yawk.wm.ui.Widget
import at.yawk.wm.x.Graphics
import java.util.ArrayList
import java.util.Collections
import javax.inject.Inject
import kotlin.math.roundToInt

@PerMonitor
@DockWidget(position = DockWidget.Position.LEFT, priority = Int.MAX_VALUE)
class ProgressWidget @Inject constructor(
    private val dock: DockBuilder,
    private val herbstClient: HerbstClient,
    private val monitor: Monitor
) : Widget() {
    private val tasks = Collections.synchronizedList(ArrayList<ProgressTask>())

    @Inject
    fun listen(manager: ProgressManager) {
        manager.addTaskCreateListener { task: ProgressTask ->
            tasks.add(task)
            task.addChangeListener {
                if (!task.isRunning) {
                    tasks.remove(task)
                }
                markDirtyAndRepaint()
            }
            markDirtyAndRepaint()
        }
    }

    private fun markDirtyAndRepaint() {
        markDirty()
        dock.render()
    }

    override fun render(graphics: Graphics) {
        if (herbstClient.currentMonitor.id == monitor.id) {
            graphics.setForegroundColor(DockConfig.activeMonitorColor.awt)
            graphics.fillRect(0, 0, dock.window.width, 1)
        }
        graphics.setForegroundColor(DockConfig.progressColor.awt)
        val y = 0
        for (task in tasks) {
            graphics.fillRect(0, y, (dock.window.width * task.progress).roundToInt(), 1)
        }
    }

    init {
        // we have 0 size in the layout
        width = 0
        height = 0
    }
}