package at.yawk.wm.dashboard

import at.yawk.wm.PeriodBuilder
import at.yawk.wm.TimedCache
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.TextWidget
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val TEMPERATURE_PATTERN = "-?\\d+(\\.\\d+)?°C".toPattern()

@PerMonitor
class TemperatureWidget @Inject internal constructor(
    fontSource: FontSource,
    private val cacheHolder: CacheHolder,
    periodBuilder: PeriodBuilder
) : TextWidget() {
    init {
        font = fontSource.getFont(DashboardConfig.temperatureFont)
        periodBuilder.submit(::refresh, TimeUnit.MINUTES.toMillis(1).toInt(), render = true)
    }

    fun refresh() {
        text = "Error"
        text = cacheHolder.cache.get()
    }

    @Singleton
    internal class CacheHolder @Inject constructor() {
        val cache = TimedCache<String>(50, TimeUnit.SECONDS) {
            Socket().use { socket ->
                socket.soTimeout = 1000
                socket.connect(InetSocketAddress("ente.hawo.stw.uni-erlangen.de", 7337))
                val response = InputStreamReader(socket.inputStream).readText()

                val matcher = TEMPERATURE_PATTERN.matcher(response)
                matcher.find()

                matcher.group()
            }
        }
    }
}