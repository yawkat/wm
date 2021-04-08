package at.yawk.wm.dock.module

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.PeriodBuilder
import at.yawk.wm.dock.Dock
import at.yawk.wm.hl.Monitor
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.IconWidget
import at.yawk.wm.ui.Origin
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.ui.TextWidget
import at.yawk.wm.ui.Widget
import at.yawk.wm.x.GlobalResourceRegistry
import at.yawk.wm.x.Window
import at.yawk.wm.x.XcbConnector
import org.slf4j.LoggerFactory
import java.util.Comparator
import java.util.function.Consumer
import javax.inject.Inject

/**
 * @author yawkat
 */
@PerMonitor
class DockBuilder @Inject constructor(
    private val monitor: Monitor,
    private val xcbConnector: XcbConnector,
    private val globalResourceRegistry: GlobalResourceRegistry
) : RenderElf {
    private lateinit var dock: Dock

    private lateinit var periodBuilder: PeriodBuilder

    fun start(bootstrap: DockBootstrap) {
        log.info("Initializing dock...")
        this.periodBuilder = bootstrap.periodBuilder
        dock = Dock(xcbConnector.screen, DockConfig.background.awt)
        globalResourceRegistry.register(dock)
        dock.setBounds(monitor.x, monitor.y, monitor.width, DockConfig.height)
        setupWidgets(bootstrap)
        bootstrap.dockStartListeners.forEach(Consumer { obj: Runnable -> obj.run() })
        dock.show()
        log.info("Dock initialized")
    }

    private fun decorate(widget: Widget) {
        when (widget) {
            is TextWidget -> widget.textHeight = DockConfig.height
            is IconWidget -> widget.targetHeight = DockConfig.height
            is FlowCompositeWidget ->
                widget.widgets.forEach { decorate(it) }
        }
    }

    fun addLeft(widget: Widget) {
        widget.origin = Origin.TOP_LEFT
        decorate(widget)
        dock.left.addWidget(widget)
    }

    fun addRight(widget: Widget) {
        widget.origin = Origin.TOP_RIGHT
        decorate(widget)
        dock.right.addWidget(widget)
    }

    override fun render() {
        if (::dock.isInitialized) {
            dock.render()
        }
    }

    //////// SETUP ////////
    private fun setupWidgets(bootstrap: DockBootstrap) {
        var widgets = bootstrap.widgets
        log.info("Visiting {} widgets...", widgets.size)
        widgets = widgets.sortedWith(Comparator.comparingInt { it.javaClass.getAnnotation(DockWidget::class.java).priority })
        for (widget in widgets) {
            val widgetClass: Class<out Widget> = widget.javaClass
            val annotation = widgetClass.getAnnotation(DockWidget::class.java)
            val position: DockWidget.Position = annotation.position
            if (position == DockWidget.Position.LEFT) {
                addLeft(widget)
            } else {
                addRight(widget)
            }
            widget.init()
        }
        periodBuilder.flush()
    }

    val window: Window
        get() = dock.window

    companion object {
        private val log = LoggerFactory.getLogger(DockBuilder::class.java)
    }
}