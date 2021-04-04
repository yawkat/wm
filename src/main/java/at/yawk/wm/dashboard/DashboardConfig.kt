package at.yawk.wm.dashboard

import at.yawk.wm.style.Color
import at.yawk.wm.style.StyleConfig

object DashboardConfig {
    val temperatureFont = StyleConfig.dashboardMain
    val mediaFont = StyleConfig.dashboardMedia
    val pingFont = StyleConfig.dashboardMedia
    val xkcdFont = StyleConfig.dashboardXkcd
    val xkcdWhite = Color.Solarized.base03
    val xkcdBlack = Color.Solarized.base01
    val pingDestinations = mapOf(
        "ente" to "ente.hawo.stw.uni-erlangen.de",
        "yawk" to "yawk.at",
        "ps-18-1" to "ps-18-1.yawk.at"
    )
}