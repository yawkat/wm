package at.yawk.wm.tac.launcher

import com.fasterxml.jackson.databind.JsonNode

/**
 * @author yawkat
 */
data class LauncherConfig(
        val shortcuts: Map<String, JsonNode>,
        val shutdownCommand: Array<String>
)