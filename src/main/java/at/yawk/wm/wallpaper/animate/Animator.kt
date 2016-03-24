package at.yawk.wm.wallpaper.animate

import at.yawk.wm.Scheduler
import at.yawk.wm.hl.Monitor
import at.yawk.wm.x.*
import at.yawk.wm.x.image.ByteArrayImage
import at.yawk.wm.x.image.LocalImage
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
        val window: Window,
        val monitors: List<Monitor>
) : AbstractResource() {

    private val graphics: Graphics

    private var currentRunningTask: Future<*>? = null

    private val canvasWidth: Int
    private val canvasHeight: Int
    private var pixMap: PixMap? = null

    init {
        graphics = window.createGraphics()
        canvasWidth = wallpaper.baseFrame.width
        canvasHeight = wallpaper.baseFrame.height
    }

    private fun drawBase() {
        // set the window background to the base frame
        val frame = wallpaper.baseFrame

        pixMap = window.createPixMap(window.screen.width, window.screen.height)

        pixMap!!.createGraphics().use { pixMapGraphics ->
            pixMapGraphics.setForegroundColor(backgroundColor)
            pixMapGraphics.setBackgroundColor(backgroundColor)
            pixMapGraphics.fillRect(0, 0, window.width, window.height)
            putFrame(pixMapGraphics, frame)
        }

        window.setBackgroundPixMap(pixMap)
        window.clear()
    }

    private fun putFrame(graphics: Graphics, frame: Frame) {
        for (monitor in monitors) {
            graphics.putImage(
                    monitor.x + (monitor.width - canvasWidth) / 2 + frame.x,
                    monitor.y + (monitor.height - canvasHeight) / 2 + frame.y,
                    ByteArrayImage(frame.width, frame.height, frame.data, 0, 3)
            )
        }
    }

    internal fun drawImage(image: LocalImage, x: Int, y: Int) {
        pixMap!!.createGraphics().use { graphics -> graphics.putImage(x, y, image) }
        window.clear()
    }

    fun start(): Future<*> {
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
                    putFrame(graphics, nextFrame)
                    graphics.flush()
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
        graphics.close()
    }
}
