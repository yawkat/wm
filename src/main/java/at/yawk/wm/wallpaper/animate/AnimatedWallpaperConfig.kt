package at.yawk.wm.wallpaper.animate

import at.yawk.wm.style.Color
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author yawkat
 */
object AnimatedWallpaperConfig {
    val cache: Path = Paths.get(".cache/wallpaper")
    val input: Path = Paths.get("wallpaper")
    val backgroundColor = Color.Solarized.base03
    const val show = false
}
