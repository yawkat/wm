package at.yawk.wm.tac.launcher

/**
 * @author yawkat
 */
data class LauncherConfig(
        val shortcuts: Map<String, Command>,
        val shutdownCommand: List<String>
)