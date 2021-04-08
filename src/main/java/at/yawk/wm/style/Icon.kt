package at.yawk.wm.style

enum class Icon(val category: String, val id: String) {
    cpu("hardware", "memory"),
    battery_0("device", "battery_alert"),
    battery_20("device", "battery_20"),
    battery_30("device", "battery_30"),
    battery_50("device", "battery_50"),
    battery_60("device", "battery_60"),
    battery_80("device", "battery_80"),
    battery_90("device", "battery_90"),
    battery_100("device", "battery_full"),
    battery_20_charging("device", "battery_charging_20"),
    battery_30_charging("device", "battery_charging_30"),
    battery_50_charging("device", "battery_charging_50"),
    battery_60_charging("device", "battery_charging_60"),
    battery_80_charging("device", "battery_charging_80"),
    battery_90_charging("device", "battery_charging_90"),
    battery_100_charging("device", "battery_charging_full"),
    net_online("action", "settings_ethernet"),
    net_offline("av", "not_interested");

    companion object {
        val HASH = values().map { it.id }.hashCode()
    }
}