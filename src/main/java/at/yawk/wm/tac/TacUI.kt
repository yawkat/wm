package at.yawk.wm.tac

import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.hl.Monitor
import at.yawk.wm.x.AbstractResource
import at.yawk.wm.x.EventGroup
import at.yawk.wm.x.Graphics
import at.yawk.wm.x.Window
import at.yawk.wm.x.WindowType
import at.yawk.wm.x.XcbConnector
import at.yawk.wm.x.event.ExposeEvent
import at.yawk.wm.x.event.FocusLostEvent
import at.yawk.wm.x.event.KeyPressEvent
import at.yawk.wm.x.font.FontCache
import at.yawk.wm.x.font.GlyphFont
import java.util.ArrayList
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Stream

class TacUI(
    fontCache: FontCache,
    private val connector: XcbConnector,
    monitor: Monitor
) : AbstractResource(), Modal {
    private val x: Int = monitor.x + monitor.width - TacConfig.width
    private val y: Int = DockConfig.height
    private val width: Int = TacConfig.width
    private var window: Window? = null
    private var graphics: Graphics? = null
    private var lastEntries: List<EntryState> = emptyList()
    var entries: List<Entry> = emptyList()
        private set
    private val primaryNormal: GlyphFont = fontCache.getFont(TacConfig.fontPrimary)
    private val secondaryNormal: GlyphFont = fontCache.getFont(TacConfig.fontSecondary)
    private val primarySelected: GlyphFont = fontCache.getFont(TacConfig.fontPrimarySelected)
    private val secondarySelected: GlyphFont = fontCache.getFont(TacConfig.fontSecondarySelected)
    private val features: MutableList<Feature> = ArrayList()

    fun setEntries(entries: Stream<out Entry>) {
        var stream = entries
        for (feature in features) {
            stream = feature.setEntries(stream, ENTRY_LIMIT)
        }
        this.entries = stream.limit(ENTRY_LIMIT.toLong()).collect(Collectors.toList())
        features.forEach(Consumer { obj: Feature -> obj.onEntriesSet() })
        render(false)
    }

    @Synchronized
    private fun render(expose: Boolean) {
        val newHeight = entries.size * TacConfig.rowHeight
        if (window == null) {
            val window = connector.screen.createWindow(
                EventGroup.PAINT, EventGroup.FOCUS, EventGroup.KEYBOARD
            )
            this.window = window
            window.addListener(ExposeEvent::class.java) {
                window.acquireFocus()
                render(true)
            }
            window.addListener(KeyPressEvent::class.java) { evt: KeyPressEvent ->
                for (feature in features) {
                    feature.onKeyPress(evt)
                    if (evt.isCancelled) {
                        break
                    }
                }
            }
            val startTime = System.currentTimeMillis()
            window.addListener(FocusLostEvent::class.java) {
                // HACK: sometimes a focus lost event would be received immediately after expose,
                // closing the UI
                if (startTime >= System.currentTimeMillis() - 100) {
                    return@addListener
                }
                close()
            }
            graphics = window.createGraphics()
            window.setBackgroundColor(TacConfig.colorBackground.awt)
                .setBounds(x, y, width, newHeight)
                .setType(WindowType.DOCK)
                .setStrutPartial(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                .show()
            return  // wait for expose
        }
        val lastHeight = lastEntries.size * TacConfig.rowHeight
        if (lastHeight != newHeight) {
            window!!.setBounds(x, y, width, newHeight)
        }
        for (i in entries.indices) {
            val entry = entries[i].state
            if (!expose && lastEntries.size > i && lastEntries[i] == entry) {
                // can skip that render
                continue
            }
            val y = i * TacConfig.rowHeight
            val background = if (entry.isSelected) TacConfig.colorSelected else TacConfig.colorBackground
            graphics!!.setForegroundColor(background.awt)
            graphics!!.fillRect(0, y, width, TacConfig.rowHeight)
            val font = if (entry.isLowPriority) {
                if (entry.isSelected) secondarySelected else secondaryNormal
            } else {
                if (entry.isSelected) primarySelected else primaryNormal
            }
            graphics!!.setFont(font)
            val h = font.getStringBounds(entry.getText()).height
            graphics!!.drawText(TacConfig.padding, y + (TacConfig.rowHeight - h) / 2, entry.getText())
        }
        graphics!!.flush()
        lastEntries = entries.stream().map { e: Entry -> e.state }.collect(Collectors.toList())
    }

    fun update() {
        render(false)
    }

    override fun close() {
        if (window != null) {
            window!!.close()
            window = null
            closeListeners.forEach(Consumer { obj: Runnable -> obj.run() })
        }
    }

    private val closeListeners: MutableList<Runnable> = ArrayList()
    override fun addCloseListener(listener: Runnable) {
        closeListeners.add(listener)
    }

    fun addFeature(feature: Feature) {
        feature.onAdd(this)
        features.add(feature)
    }

    companion object {
        private const val ENTRY_LIMIT = 20
    }

    init {
        addFeature(CloseFeature())
    }
}