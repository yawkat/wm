package at.yawk.wm.tac.password

import at.yawk.wm.style.Color
import at.yawk.wm.style.StyleConfig
import java.nio.file.Paths

object PasswordConfig {
        val cacheDir = Paths.get("/home/yawkat/.local/share/password")
        /**
         * Password storage timeout in seconds.
         */
        val timeout = 600
        val remote = "https://pw.yawk.at"
        val editorBackground = Color.Solarized.base03
        val editorFont = StyleConfig.base0
        const val editorWidth = 400
        const val editorHeight = 400
}
