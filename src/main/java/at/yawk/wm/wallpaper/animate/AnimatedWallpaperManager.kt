package at.yawk.wm.wallpaper.animate

import at.yawk.wm.Scheduler
import at.yawk.wm.dashboard.DesktopManager
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.x.XcbConnector
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.Files
import java.util.concurrent.Future
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author yawkat
 */
@Singleton
class AnimatedWallpaperManager @Inject constructor(
        val connector: XcbConnector,
        val scheduler: Scheduler,
        val wallpaperConfig: AnimatedWallpaperConfig,
        val herbstClient: HerbstClient,
        val desktopManager: DesktopManager
) {
    private var animator: Animator? = null

    fun start() {
        log.info("Initializing wallpaper...")

        var wallpaper: AnimatedWallpaper? = null

        if (Files.exists(wallpaperConfig.cache)) {
            val cacheMod = Files.getLastModifiedTime(wallpaperConfig.cache)
            val inMod = Files.getLastModifiedTime(wallpaperConfig.input)
            if (cacheMod.compareTo(inMod) >= 0) {
                log.info("Found valid cached wallpaper animation")
                DataInputStream(Files.newInputStream(wallpaperConfig.cache)).use { `in` -> wallpaper = AnimatedWallpaper.read(`in`) }
                log.info("Wallpaper loaded to memory")
            }
        }

        if (wallpaper == null) {
            log.info("Need to compile the wallpaper animation, this may take a while!")
            wallpaper = AnimationBuilder.loadDirectory(wallpaperConfig.input)
            DataOutputStream(Files.newOutputStream(wallpaperConfig.cache)).use { out -> wallpaper!!.write(out) }
            log.info("Compilation complete")
        }

        show(wallpaper!!)
    }

    private fun show(wallpaper: AnimatedWallpaper) {
        animator = Animator(wallpaper, wallpaperConfig.backgroundColor, scheduler, desktopManager.getDesktops())
        animator!!.start()
    }

    fun stop(): Future<*> {
        return animator!!.stop()
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(AnimatedWallpaperManager::class.java)
    }
}
