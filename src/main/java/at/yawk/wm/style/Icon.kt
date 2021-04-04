package at.yawk.wm.style

enum class Icon(val subPath: String) {
    cpu("hardware/1x_web/ic_memory_white_18dp.png"),
    battery_0("device/1x_web/ic_battery_alert_white_18dp.png"),
    battery_20("device/1x_web/ic_battery_20_white_18dp.png"),
    battery_30("device/1x_web/ic_battery_30_white_18dp.png"),
    battery_50("device/1x_web/ic_battery_50_white_18dp.png"),
    battery_60("device/1x_web/ic_battery_60_white_18dp.png"),
    battery_80("device/1x_web/ic_battery_80_white_18dp.png"),
    battery_90("device/1x_web/ic_battery_90_white_18dp.png"),
    battery_100("device/1x_web/ic_battery_full_white_18dp.png"),
    battery_20_charging("device/1x_web/ic_battery_charging_20_white_18dp.png"),
    battery_30_charging("device/1x_web/ic_battery_charging_30_white_18dp.png"),
    battery_50_charging("device/1x_web/ic_battery_charging_50_white_18dp.png"),
    battery_60_charging("device/1x_web/ic_battery_charging_60_white_18dp.png"),
    battery_80_charging("device/1x_web/ic_battery_charging_80_white_18dp.png"),
    battery_90_charging("device/1x_web/ic_battery_charging_90_white_18dp.png"),
    battery_100_charging("device/1x_web/ic_battery_charging_full_white_18dp.png"),
    net_online("action/1x_web/ic_settings_ethernet_white_18dp.png"),
    net_offline("av/1x_web/ic_not_interested_white_18dp.png"),
    playing("av/1x_web/ic_pause_white_18dp.png"),
    paused("av/1x_web/ic_play_arrow_white_18dp.png");

    companion object {
        val HASH = values().map { it.subPath }.hashCode()
    }
}