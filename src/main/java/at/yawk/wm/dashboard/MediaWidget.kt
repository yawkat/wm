package at.yawk.wm.dashboard

import at.yawk.dbus.protocol.`object`.DbusObject
import at.yawk.wm.dbus.MediaPlayer
import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.ui.TextWidget
import at.yawk.wm.x.Graphics
import org.slf4j.LoggerFactory
import java.util.concurrent.Executor
import javax.inject.Inject

private val log = LoggerFactory.getLogger(MediaWidget::class.java)

@PerMonitor
class MediaWidget @Inject constructor(
        val fontSource: FontSource,
        val mediaPlayer: MediaPlayer,
        val renderElf: RenderElf,
        val executor: Executor
) : FlowCompositeWidget() {
    val title = TextWidget()
    val artist = TextWidget()
    val album = TextWidget()

    init {
        val font = fontSource.getFont(DashboardConfig.mediaFont)

        addWidget(title)
        addWidget(artist)
        addWidget(album)

        title.after(artist, Direction.VERTICAL)
        artist.after(album, Direction.VERTICAL)
        album.after(anchor, Direction.VERTICAL)

        title.font = font
        artist.font = font
        album.font = font

        mediaPlayer.onPropertiesChanged { executor.execute { update() } }
    }

    override fun init() {
        update()
    }

    fun update() {
        val metadata = try {
            mediaPlayer.metadata
        } catch(e: Exception) {
            // dbus error
            log.error("Failed to get metadata")
            emptyMap<String, DbusObject>()
        }
        title.text = metadata["xesam:title"]?.stringValue() ?: ""
        artist.text = (metadata["xesam:artist"]?.values ?: emptyList()).map { it.stringValue() }.joinToString(", ")
        album.text = metadata["xesam:album"]?.stringValue() ?: ""
        renderElf.render()
    }

    override fun layout(graphics: Graphics) {
        widgets.forEach { it.x = this.x }
        super.layout(graphics)
    }
}