package at.yawk.wm.dock.module.widget

import at.yawk.wm.di.PerMonitor
import at.yawk.wm.dbus.MediaPlayer
import at.yawk.wm.dock.module.DockConfig.mediaFont
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.IconWidget
import javax.inject.Inject

@PerMonitor
@DockWidget(position = DockWidget.Position.RIGHT, priority = 210)
class MediaWidget @Inject constructor(
    private val mediaPlayer: MediaPlayer,
    private val herbstClient: HerbstClient
) : FlowCompositeWidget() {

    private lateinit var playing: IconWidget // todo

    override fun init() {
        playing = IconWidget()
        playing.setColor(mediaFont)
        playing.after(anchor, Direction.HORIZONTAL)
        addWidget(playing)
        herbstClient.addKeyHandler("Mod4-Pause") { mediaPlayer.playPause() }
        herbstClient.addKeyHandler("Mod4-Insert") { mediaPlayer.previous() }
        herbstClient.addKeyHandler("Mod4-Delete") { mediaPlayer.next() }
        herbstClient.addKeyHandler("XF86AudioPlay") { mediaPlayer.playPause() }
        herbstClient.addKeyHandler("XF86AudioStop") { mediaPlayer.stop() }
        herbstClient.addKeyHandler("XF86AudioPrevious") { mediaPlayer.previous() }
        herbstClient.addKeyHandler("XF86AudioNext") { mediaPlayer.next() }
    }
}