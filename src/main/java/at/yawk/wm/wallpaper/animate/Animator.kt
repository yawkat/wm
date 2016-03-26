package at.yawk.wm.wallpaper.animate

import at.yawk.wm.Scheduler
import at.yawk.wm.dashboard.DesktopManager
import at.yawk.wm.x.AbstractResource
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.PixMap
import at.yawk.wm.x.image.ByteArrayImage
import at.yawk.wm.x.use
import java.awt.Color
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * @author yawkat
 */
class Animator(
        var wallpaper: AnimatedWallpaper,
        val backgroundColor: Color,
        val scheduler: Scheduler,
        desktops: Collection<DesktopManager.Desktop>
) : AbstractResource() {

    private val targets = desktops.map { Target(it) }

    private var currentRunningTask: Future<*>? = null

    private val canvasWidth = wallpaper.baseFrame.width
    private val canvasHeight = wallpaper.baseFrame.height

    private fun drawBase() {
        // set the window background to the base frame
        val frame = wallpaper.baseFrame

        for (target in targets) {
            target.putBaseFrame(frame)
        }
    }

    fun start(): Future<*> {
        targets.forEach { it.init() }
        drawBase()
        wallpaper = wallpaper.copy(baseFrame = Frame.EMPTY_FRAME)
        return show(wallpaper.start)
    }

    fun stop(): Future<*> {
        return show(wallpaper.stop)
    }

    /**
     * @return A future that completes when the animation is done.
     */
    @Synchronized private fun show(animation: FrameAnimation): Future<*> {
        stopTask()

        currentRunningTask = scheduler.scheduleAtFixedRate(
                FrameRunner(animation.frames),
                0, animation.interval, TimeUnit.MILLISECONDS)
        return currentRunningTask!!
    }

    private inner class FrameRunner(var remainingFrames: List<Frame>) : Runnable {
        override fun run() {
            if (remainingFrames.isNotEmpty()) {
                val nextFrame = remainingFrames[0]
                if (!nextFrame.isEmpty()) {
                    for (target in targets) {
                        target.putAnimationFrame(nextFrame)
                    }
                }
                remainingFrames = remainingFrames.drop(1) // remove iterator to allow GC
            } else {
                stopTask()
            }
        }
    }

    @Synchronized private fun stopTask() {
        if (currentRunningTask != null) {
            currentRunningTask!!.cancel(false)
            currentRunningTask = null
        }
    }

    override fun close() {
        for (target in targets) {
            target.close()
        }
    }

    private inner class Target(val desktop: DesktopManager.Desktop) {
        val window = desktop.window

        lateinit var pixMap: PixMap
        lateinit var graphics: Graphics

        fun init() {
            graphics = window.createGraphics()
            pixMap = window.createPixMap(window.screen.width, window.screen.height)
        }

        fun putBaseFrame(frame: Frame) {
            pixMap.createGraphics().use {
                it.setForegroundColor(backgroundColor)
                it.setBackgroundColor(backgroundColor)
                it.fillRect(0, 0, window.width, window.height)
                putFrame(it, frame)
            }
            window.setBackgroundPixMap(pixMap)
            window.clear()
        }

        fun putAnimationFrame(frame: Frame) {
            putFrame(graphics, frame)
        }

        fun putFrame(target: Graphics, frame: Frame) {
            target.putImage(
                    (window.width - canvasWidth) / 2 + frame.x,
                    (window.height - canvasHeight) / 2 + frame.y,
                    ByteArrayImage(frame.width, frame.height, frame.data, 0, 3)
            )
        }

        fun close() {
            graphics.close()
        }
    }

    fun isAnimationRunning() = currentRunningTask != null
}
