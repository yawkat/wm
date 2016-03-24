package at.yawk.wm.wallpaper.animate

import java.io.DataInput
import java.io.DataOutput

/**
 * @author yawkat
 */
data class Frame(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        /**
         * RGB array of frame data. Always `len = 3 * width * height`.
         */
        val data: ByteArray
) {
    fun write(output: DataOutput) {
        output.writeInt(x)
        output.writeInt(y)
        output.writeInt(width)
        output.writeInt(height)
        output.write(data)
    }

    fun isEmpty(): Boolean = width == 0 || height == 0

    fun copy() = Frame(x, y, width, height, data.copyOf())

    companion object {
        val EMPTY_FRAME = Frame(x = 0, y = 0, width = 0, height = 0, data = ByteArray(0))

        fun read(input: DataInput): Frame {
            val x = input.readInt()
            val y = input.readInt()
            val width = input.readInt()
            val height = input.readInt()
            val data = ByteArray(3 * width * height)
            input.readFully(data)
            return Frame(x, y, width, height, data)
        }
    }
}
