package at.yawk.wm.tac.password

import at.yawk.wm.style.FontDescriptor
import java.awt.Color
import java.nio.file.Path

/**
 * @author yawkat
 */
data class PasswordConfig(
        val cacheDir: Path,
        /**
         * Password storage timeout in seconds.
         */
        val timeout: Int,
        val remote: String,
        val editorBackground: Color,
        val editorFont: FontDescriptor,
        val editorWidth: Int,
        val editorHeight: Int
)
