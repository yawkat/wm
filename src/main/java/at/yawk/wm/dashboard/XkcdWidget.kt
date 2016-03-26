package at.yawk.wm.dashboard

import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.hl.Monitor
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.ui.Widget
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.PixMap
import at.yawk.wm.x.XcbConnector
import at.yawk.wm.x.font.GlyphFont
import at.yawk.wm.x.image.BufferedLocalImage
import at.yawk.wm.x.use
import org.jsoup.Jsoup
import java.awt.Color
import java.net.URL
import java.util.*
import java.util.concurrent.Executor
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.inject.Singleton

/**
 * @author yawkat
 */
class XkcdWidget @Inject constructor(
        val monitor: Monitor,
        val fontSource: FontSource,
        val loader: XkcdLoader,
        val renderElf: RenderElf
) : Widget() {
    override fun init() {
        loader.subscribers += { renderElf.render() }
    }

    override fun layout(graphics: Graphics?) {
        val comic = loader.comic
        if (comic == null) {
            width = 0
            height = 0
        } else {
            width = comic.width + 10
            height = comic.height + 10
        }
    }

    override fun render(graphics: Graphics) {
        val comic = loader.comic
        if (comic != null) {
            graphics.drawPixMap(comic, Math.min(x, x2) + 5, Math.min(y, y2) + 5)
        }
    }
}

@Singleton
class XkcdLoader @Inject constructor(
        val executor: Executor,
        val xcbConnector: XcbConnector,
        val dashboardConfig: DashboardConfig,
        val fontSource: FontSource,
        val wallpaperConfig: AnimatedWallpaperConfig
) {
    internal var comic: PixMap? = null
    private lateinit var font: GlyphFont

    internal var subscribers = emptyList<() -> Unit>()

    fun start() {
        font = fontSource.getFont(dashboardConfig.xkcdFont)
        executor.execute { fetch() }
    }

    private fun fetch() {
        val document = Jsoup.connect("https://xkcd.com/").get()
        val imageTag = document.select("#comic img").first()

        val title = imageTag.attr("title")
        val url = imageTag.absUrl("src")
        val bufferedImage = ImageIO.read(URL(url))

        // copy to get ARGB
        val localImage = BufferedLocalImage(bufferedImage).copy()
        localImage.apply { rgb ->
            val hsb = Color.RGBtoHSB((rgb ushr 16) and 0xff, (rgb ushr 8) and 0xff, rgb and 0xff, null)
            val whitePart = hsb[2]
            if (whitePart > 0.99) {
                dashboardConfig.xkcdWhite.rgb
            } else if (whitePart < 0.01) {
                dashboardConfig.xkcdBlack.rgb
            } else {
                val blackPart = 1 - whitePart
                val r = dashboardConfig.xkcdBlack.red * blackPart + dashboardConfig.xkcdWhite.red * whitePart
                val g = dashboardConfig.xkcdBlack.green * blackPart + dashboardConfig.xkcdWhite.green * whitePart
                val b = dashboardConfig.xkcdBlack.blue * blackPart + dashboardConfig.xkcdWhite.blue * whitePart
                (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
            }
        }

        data class Line(var width: Int = 0, var height: Int = 0, var content: String = "")

        val titleWords = title.split(' ')
        val lines = ArrayList<Line>()

        val spaceBounds = font.getStringBounds(" ")

        var currentLine = Line()
        for (word in titleWords) {
            val bounds = font.getStringBounds(word)
            if (currentLine.width + spaceBounds.width + bounds.width > bufferedImage.width) {
                lines.add(currentLine)
                currentLine = Line(
                        bounds.width,
                        bounds.height,
                        word
                )
            } else {
                currentLine.content += " " + word
                currentLine.width += spaceBounds.width + bounds.width
                currentLine.height = Math.max(
                        Math.max(currentLine.height, bounds.height), spaceBounds.height)
            }
        }
        if (currentLine.content.isNotBlank()) {
            lines.add(currentLine)
        }

        val textWidth = (lines.map { it.width } + bufferedImage.width).max()!!
        val textHeight = lines.map { it.height }.sum()

        val window = xcbConnector.screen.rootWindow
        val pixMap = window.createPixMap(textWidth, bufferedImage.height + textHeight)
        pixMap.createGraphics().use {
            it.setBackgroundColor(wallpaperConfig.backgroundColor)
            it.setForegroundColor(wallpaperConfig.backgroundColor)
            it.fillRect(0, 0, pixMap.width, pixMap.height)

            val imagePaddingLeft = textWidth - bufferedImage.width
            it.putImage(imagePaddingLeft, 0, localImage)
            it.setFont(font)

            var y = bufferedImage.height
            for (line in lines) {
                var x = if (line.width >= bufferedImage.width) {
                    textWidth - line.width
                } else {
                    imagePaddingLeft + (bufferedImage.width - line.width) / 2
                }
                it.drawText(x, y, line.content)
                y += line.height
            }
        }

        comic = pixMap
        subscribers.forEach { it() }
    }
}