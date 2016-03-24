package at.yawk.wm.wallpaper.animate

import java.io.DataInput
import java.io.DataOutput
import java.util.*

/**
 * @author yawkat
 */
data class FrameAnimation(
        val interval: Long,
        val frames: List<Frame>
) {
    fun write(output: DataOutput) {
        output.writeLong(interval)
        output.writeInt(frames.size)
        for (frame in frames) {
            frame.write(output)
        }
    }

    companion object {
        fun read(input: DataInput): FrameAnimation {
            val interval = input.readLong()
            val frameCount = input.readInt()
            val frames = ArrayList<Frame>(frameCount)
            for (i in 0..frameCount - 1) {
                frames.add(Frame.read(input))
            }
            return FrameAnimation(interval, frames)
        }
    }
}
