package at.yawk.wm.dashboard

import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.ui.TextWidget
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private val TEMPERATURE_PATTERN = "-?\\d+(\\.\\d+)?°C".toPattern()

/**
 * @author yawkat
 */
class TemperatureWidget @Inject constructor(
        val fontSource: FontSource,
        val dashboardConfig: DashboardConfig
) : TextWidget() {
    init {
        font = fontSource.getFont(dashboardConfig.temperatureFont)
    }

    @Periodic(value = 1, unit = TimeUnit.MINUTES, render = true)
    fun refresh() {
        text = "Error"
        Socket().use { socket ->
            socket.soTimeout = 1000
            socket.connect(InetSocketAddress("ente.hawo.stw.uni-erlangen.de", 7337))
            val response = InputStreamReader(socket.inputStream).readText()

            val matcher = TEMPERATURE_PATTERN.matcher(response)
            matcher.find()
            text = matcher.group()
        }
    }
}