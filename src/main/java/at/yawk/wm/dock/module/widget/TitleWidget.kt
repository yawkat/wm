package at.yawk.wm.dock.module.widget

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.hl.HerbstEventBus
import at.yawk.wm.hl.TitleEvent
import at.yawk.wm.ui.TextWidget
import javax.inject.Inject

@PerMonitor
@DockWidget(position = DockWidget.Position.LEFT, priority = 100)
class TitleWidget @Inject constructor(private val eventBus: HerbstEventBus) : TextWidget() {
    override fun init() {
        eventBus.addTitleEventHandlers(object : TitleEvent.Handler {
            override fun handle(event: TitleEvent) {
                text = event.title
            }
        })
    }

    @Inject
    fun init(fontSource: FontSource) {
        font = fontSource.getFont(DockConfig.windowTitleFont)
    }

    init {
        z = -1000
    }
}