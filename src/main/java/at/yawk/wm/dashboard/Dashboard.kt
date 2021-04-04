package at.yawk.wm.dashboard

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.hl.Monitor
import at.yawk.wm.ui.*
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.event.ExposeEvent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author yawkat
 */
@Singleton
class Dashboard @Inject constructor(
        val monitor: Monitor,
        val desktopManager: DesktopManager,
        val periodBuilder: PeriodBuilder,
        val animatedWallpaperManager: AnimatedWallpaperManager
) : RenderElf {

    private val layoutManager = LayoutManager()

    private val topLeft = DashboardOrigin(Origin.TOP_LEFT)
    private val topRight = DashboardOrigin(Origin.TOP_RIGHT)
    private val bottomLeft = DashboardOrigin(Origin.BOTTOM_LEFT)
    private val bottomRight = DashboardOrigin(Origin.BOTTOM_RIGHT)

    private var graphics: Graphics? = null
    private val window = desktopManager.getDesktop(monitor).window

    init {
        topLeft.init()
        topRight.init()
        bottomLeft.init()
        bottomRight.init()
    }

    @Inject
    fun initWidgets(
            temperatureWidget: TemperatureWidget,
            mediaWidget: MediaWidget,
            pingWidget: PingWidget,
            xkcdWidget: XkcdWidget
    ) {
        bottomLeft.add(temperatureWidget)
        bottomLeft.add(pingWidget)

        bottomRight.add(mediaWidget)

        topRight.add(xkcdWidget)

        val widgets = listOf(temperatureWidget, mediaWidget, pingWidget, xkcdWidget)

        widgets.forEach { it.init() }

        widgets.forEach { periodBuilder.scan(it) }
        periodBuilder.flush()
    }

    fun start() {
        graphics = window.createGraphics()
        window.addListener(ExposeEvent::class.java) { render(expose = true) }
        render()
    }

    override fun render() {
        render(expose = false)
    }

    @Synchronized
    private fun render(expose: Boolean) {
        if (!animatedWallpaperManager.isAnimationRunning()) {
            window.clear()
        }
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
            anchor.y = if (origin.isTop) DockConfig.height else window.height
        }

        fun add(widget: Widget) {
            widget.origin = origin
            widget.x = if (origin.isLeft) 0 else window.width
            widgets.addWidget(widget)
        }
    }
}