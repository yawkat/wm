package at.yawk.wm.x

/**
 * @author yawkat
 */
enum class WindowType(@JvmField internal val value: String) {
    DOCK("_NET_WM_WINDOW_TYPE_DOCK"),
    DESKTOP("_NET_WM_WINDOW_TYPE_DESKTOP")
}
