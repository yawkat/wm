package at.yawk.wm.dock.module.widget

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.DockConfig.clockFont
import at.yawk.wm.dock.module.DockConfig.clockFormat
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.TextWidget
import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerMonitor
@DockWidget(position = DockWidget.Position.RIGHT, priority = 200)
class ClockWidget @Inject constructor(fontSource: FontSource, periodBuilder: PeriodBuilder) : TextWidget() {
    private val clock: Clock = Clock.systemDefaultZone()

    init {
        font = fontSource.getFont(clockFont)
        periodBuilder.submit(::update, TimeUnit.SECONDS.toMillis(1).toInt(), true)
    }

    private fun update() {
        text = clockFormat.format(LocalDateTime.now(clock))
    }
}