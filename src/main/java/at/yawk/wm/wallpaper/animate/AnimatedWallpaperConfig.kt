package at.yawk.wm.wallpaper.animate

import java.awt.Color
import java.nio.file.Path

/**
 * @author yawkat
 */
data class AnimatedWallpaperConfig(
        val cache: Path,
        val input: Path,
        val backgroundColor: Color,
        val show: Boolean
)
