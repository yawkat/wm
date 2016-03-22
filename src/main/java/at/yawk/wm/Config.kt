package at.yawk.wm

import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.style.StyleConfig
import at.yawk.wm.tac.TacConfig
import at.yawk.wm.tac.launcher.LauncherConfig
import at.yawk.wm.tac.password.PasswordConfig
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig
import at.yawk.wm.x.icon.IconConfig

/**
 * @author yawkat
 */
internal data class Config(
        val style: StyleConfig,
        val dock: DockConfig,
        val tac: TacConfig,
        val launcher: LauncherConfig,
        val password: PasswordConfig,
        val wallpaper: AnimatedWallpaperConfig,
        val paste: at.yawk.paste.client.Config,
        val icon: IconConfig
)
