package at.yawk.wm.wallpaper.animate

import java.io.DataInput
import java.io.DataOutput

/**
 * @author yawkat
 */
data class AnimatedWallpaper(
        val baseFrame: Frame,
        val start: FrameAnimation,
        val stop: FrameAnimation
) {
    fun write(output: DataOutput) {
        baseFrame.write(output)
        start.write(output)
        stop.write(output)
    }

    companion object {
        fun read(input: DataInput): AnimatedWallpaper {
            return AnimatedWallpaper(
                    baseFrame = Frame.read(input),
                    start = FrameAnimation.read(input),
                    stop = FrameAnimation.read(input)
            )
        }
    }
}
