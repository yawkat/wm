package at.yawk.wm.wallpaper.animate

import at.yawk.wm.Util
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern

private val EMPTY_FRAME = Frame(x = 0, y = 0, width = 0, height = 0, data = ByteArray(0))
/**
 * @author yawkat
 */
internal object AnimationBuilder {

    private data class ImageHolder(val time: Long, val image: BufferedImage)

    /**
     * This method loads animations from a directory of the following format:

     *
     * base.png
     * start100.png
     * start200.png
     * start300.png
     * [...]
     * stop100.png
     * stop200.png
     * stop300.png
     * [...]
     *

     * The number represents the milliseconds when to show the frame. Every image must have the same size.
     */
    fun loadDirectory(animationDirectory: Path): AnimatedWallpaper {
        val base = Util.loadImage(animationDirectory.resolve("base.png"))

        val baseFrame = createFrame(base, null, 0, 0, base.width, base.height)

        val start = ArrayList<ImageHolder>()
        val stop = ArrayList<ImageHolder>()

        val framePattern = Pattern.compile("(start|stop)(\\d+)\\.png")

        val files = Files.list(animationDirectory)
        for (path in Iterable { files.iterator() }) {
            val matcher = framePattern.matcher(path.fileName.toString())
            if (matcher.matches()) {
                val time = java.lang.Long.parseUnsignedLong(matcher.group(2))
                val holder = ImageHolder(time, Util.loadImage(path))
                (if (matcher.group(1) == "start") start else stop).add(holder)
            }
        }

        return AnimatedWallpaper(
                baseFrame = baseFrame,
                start = linkAnimations(baseFrame, start),
                stop = linkAnimations(baseFrame, stop)
        )
    }

    private fun linkAnimations(background: Frame, images: MutableList<ImageHolder>): FrameAnimation {
        images.sortBy { it.time }

        var interval: Long? = null
        for (image in images) {
            if (image.time == 0L) {
                continue
            }
            interval = if (interval == null) image.time else gcd(interval, image.time)
        }

        val frames = ArrayList<Frame>()
        val screen = background.copy()
        for (image in images) {
            val diff = diffFrame(screen, image.image)

            if (!diff.isEmpty()) {
                val i = (image.time / interval!!).toInt()
                // fill up with empty frames
                for (j in frames.size..i) {
                    frames.add(EMPTY_FRAME)
                }

                // add our frame
                frames[i] = diff

                paint(screen, diff)
            }
        }

        return FrameAnimation(interval!!, frames)
    }

    private fun gcd(a: Long, b: Long): Long {
        return if (b == 0L) a else gcd(b, a % b)
    }

    /**
     * Create a frame that represents the difference of the two input images. Pixel data will come from the front
     * image.
     */
    private fun diffFrame(back: Frame, front: BufferedImage): Frame {
        var minX = back.width
        var minY = back.height
        var maxX = -1
        var maxY = -1
        for (x in 0..back.width - 1) {
            for (y in 0..back.height - 1) {
                val f = front.getRGB(x, y)
                if (f.ushr(24) != 0) {
                    val bgOffset = (x + y * back.width) * 3
                    val write = (f shr 16 and 0xff != back.data[bgOffset].toInt() and 0xff) or
                            (f shr 8 and 0xff != back.data[bgOffset + 1].toInt() and 0xff) or
                            (f and 0xff != back.data[bgOffset + 2].toInt() and 0xff)

                    if (write) {
                        minX = Math.min(minX, x)
                        minY = Math.min(minY, y)
                        maxX = Math.max(maxX, x)
                        maxY = Math.max(maxY, y)
                    }

                } // else foreground has 0 alpha, ignore pixel
            }
        }

        if (maxX < minX || maxY < minY) {
            // no pixels to copy
            return EMPTY_FRAME
        } else {
            return createFrame(front, back, minX, minY, maxX - minX + 1, maxY - minY + 1)
        }
    }

    private fun createFrame(image: BufferedImage, bg: Frame?,
                            startX: Int, startY: Int, width: Int, height: Int): Frame {
        val data = ByteArray(width * height * 3)
        for (x in 0..width - 1) {
            for (y in 0..height - 1) {
                val rgba = image.getRGB(x + startX, y + startY)

                val a = (rgba ushr 24) and 0xff
                var r = (rgba ushr 16) and 0xff
                var g = (rgba ushr 8) and 0xff
                var b = rgba and 0xff

                if (a != 0xff) {
                    val weightFront = (a / 0xffD).toDouble()
                    val weightBack = 1 - weightFront

                    var backR = 0
                    var backG = 0
                    var backB = 0
                    if (bg != null) {
                        val bgOffset = (x + startX + (y + startY) * bg.width) * 3
                        backR = bg.data[bgOffset].toInt() and 0xff
                        backG = bg.data[bgOffset + 1].toInt() and 0xff
                        backB = bg.data[bgOffset + 2].toInt() and 0xff
                    }

                    r = (r * weightFront + backR * weightBack).toInt()
                    g = (g * weightFront + backG * weightBack).toInt()
                    b = (b * weightFront + backB * weightBack).toInt()
                }

                val offset = (x + y * width) * 3
                data[offset] = r.toByte()
                data[offset + 1] = g.toByte()
                data[offset + 2] = b.toByte()
            }
        }
        return Frame(
                startX, startY,
                width, height,
                data)
    }

    /**
     * Paint the second frame onto the canvas frame, modifying the former.
     */
    private fun paint(canvas: Frame, frame: Frame) {
        for (y in 0..frame.height - 1) {
            val frontOffset = y * frame.width * 3
            val backOffset = (frame.x - canvas.x + (y + frame.y - canvas.y) * canvas.width) * 3
            System.arraycopy(frame.data, frontOffset, canvas.data, backOffset, 3 * frame.width)
        }
    }
}
