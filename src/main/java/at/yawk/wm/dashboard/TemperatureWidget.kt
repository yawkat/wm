package at.yawk.wm.dashboard

import at.yawk.wm.TimedCache
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.dock.module.Periodic
import at.yawk.wm.ui.TextWidget
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val TEMPERATURE_PATTERN = "-?\\d+(\\.\\d+)?°C".toPattern()

/**
 * @author yawkat
 */
class TemperatureWidget @Inject internal constructor(
        val fontSource: FontSource,
        val cacheHolder: CacheHolder
) : TextWidget() {
    init {
        font = fontSource.getFont(DashboardConfig.temperatureFont)
    }

    @Periodic(value = 1, unit = TimeUnit.MINUTES, render = true)
    fun refresh() {
        text = "Error"
        text = cacheHolder.cache.get()
    }

    @Singleton
    internal class CacheHolder {
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