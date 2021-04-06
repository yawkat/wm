package at.yawk.wm.dashboard

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.di.PerMonitor
import javax.inject.Inject

@PerMonitor
class DashboardBootstrap @Inject constructor(
    private val dashboard: Dashboard,
    temperatureWidget: TemperatureWidget,
    mediaWidget: MediaWidget,
    pingWidget: PingWidget,
    xkcdWidget: XkcdWidget,
    periodBuilder: PeriodBuilder
) {
    init {
        dashboard.initWidgets(temperatureWidget, mediaWidget, pingWidget, xkcdWidget, periodBuilder)
    }

    fun start() {
        dashboard.start()
    }
}