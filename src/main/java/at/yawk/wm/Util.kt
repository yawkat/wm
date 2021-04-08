package at.yawk.wm

import org.graalvm.nativeimage.ImageInfo
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

/**
 * @author yawkat
 */
object Util {
    /**
     * @param tryMaxCount The maximum output length
     */
    fun split(input: String, delimiter: Char, tryMaxCount: Int): List<String> {
        val found = ArrayList<String>(if (tryMaxCount > 4) 4 else tryMaxCount)
        val currentEntry = StringBuilder()
        for (i in 0..input.length - 1) {
            val c = input[i]
            if (c == delimiter) {
                if (currentEntry.length != 0) {
                    found.add(currentEntry.toString())
                    currentEntry.setLength(0)
                    if (found.size >= tryMaxCount) {
                        break
                    }
                }
            } else {
                currentEntry.append(c)
            }
        }
        if (found.size < tryMaxCount && currentEntry.length > 0) {
            found.add(currentEntry.toString())
        }
        return found
    }

    /**
     * Taken from guava, this returns 0 for a/A, 25 for z/Z and a larger value for any non-letter.
     */
    fun alphabetIndex(c: Char): Int {
        return (c.toInt() or 0x20) - 'a'.toInt()
    }

    fun startsWithIgnoreCaseAscii(s: String, prefix: String): Boolean {
        if (prefix.length > s.length) {
            return false
        }
        for (i in 0..prefix.length - 1) {
            val o = s[i]
            val p = prefix[i]
            if (o != p) {
                val ao = alphabetIndex(o)
                if (ao > 26 || ao != alphabetIndex(p)) {
                    return false
                }
            }
        }
        return true
    }

    fun containsIgnoreCaseAscii(haystack: String, needle: String): Boolean {
        if (needle.length > haystack.length) {
            return false
        }

        outer@ for (i in 0..haystack.length - needle.length) {
            for (j in 0..needle.length - 1) {
                val ch = haystack[i + j]
                val cn = needle[j]
                if (ch != cn) {
                    val ih = alphabetIndex(ch)
                    val `in` = alphabetIndex(cn)
                    if (ih != `in` || ih >= 26) {
                        // no match
                        continue@outer
                    }
                }
            }
            // match, return
            return true
        }
        return false
    }

    fun loadImage(path: Path): BufferedImage {
        Files.newInputStream(path).use { `in` -> return ImageIO.read(`in`) }
    }

    fun streamToString(stream: InputStream, blockSize: Int): String {
        val buf = ByteArrayOutputStream()
        val bytes = ByteArray(blockSize)
        var len: Int
        while (true) {
            len = stream.read(bytes)
            if (len == -1) break
            buf.write(bytes, 0, len)
        }
        return buf.toString("UTF-8")
    }

    var emulateAtBuildTime = true

    val inBuildTime: Boolean
        @JvmStatic get() = if (ImageInfo.inImageCode()) ImageInfo.inImageBuildtimeCode() else emulateAtBuildTime

    @JvmStatic
    fun requireRuntime() {
        check(!inBuildTime) { "Must only be run at runtime" }
    }

    @JvmStatic
    fun requireBuildTime() {
        check(inBuildTime) { "Must only be run at build time" }
    }
}
