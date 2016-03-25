package at.yawk.wm.dashboard

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.hl.Monitor
import at.yawk.wm.style.NamedFontDescriptor
import at.yawk.wm.ui.*
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.event.ExposeEvent
import at.yawk.wm.x.font.FontCache
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author yawkat
 */
@Singleton
class Dashboard @Inject constructor(
        val monitor: Monitor,
        val fontCache: FontCache,
        val desktopManager: DesktopManager,
        val dockConfig: DockConfig,
        val periodBuilder: PeriodBuilder
) : RenderElf {

    private val layoutManager = LayoutManager()
    private val bottomLeft = DashboardOrigin(Origin.BOTTOM_LEFT)

    private var graphics: Graphics? = null
    private val window = desktopManager.getDesktop(monitor).window

    @Inject
    fun initWidgets(
            temperatureWidget: TemperatureWidget
    ) {
        bottomLeft.add(temperatureWidget)

        periodBuilder.scan(temperatureWidget)

        periodBuilder.flush()
    }

    fun start() {
        bottomLeft.init()

        graphics = window.createGraphics()
        window.addListener(ExposeEvent::class.java) { render(expose = true) }
        render()
    }

    override fun render() {
        render(expose = false)
    }

    @Synchronized
    private fun render(expose: Boolean) {
        val graphics = graphics
        if (graphics != null) {
            layoutManager.render(RenderPass(graphics, expose))
        }
    }

    private inner class DashboardOrigin(val origin: Origin) {
        val anchor = Anchor()
        val widgets = DirectionalWidgetChain(layoutManager, anchor, Direction.VERTICAL)

        fun init() {
            anchor.x = if (origin.isLeft) 0 else window.width
            anchor.y = if (origin.isTop) dockConfig.height else window.height
        }

        fun add(widget: Widget) {
            widget.origin = origin
            widgets.addWidget(widget)
        }
    }
}