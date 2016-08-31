package at.yawk.wm.tac.launcher

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author yawkat
 */
data class Command @JvmOverloads constructor(
        val command: List<String>,
        @JsonProperty("jail") val jailOptions: List<String>? = null
)