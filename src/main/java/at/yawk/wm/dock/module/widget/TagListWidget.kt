package at.yawk.wm.dock.module.widget

import at.yawk.wm.dock.module.DockConfig
import at.yawk.wm.dock.module.DockWidget
import at.yawk.wm.dock.module.FontSource
import at.yawk.wm.hl.HerbstClient
import at.yawk.wm.hl.HerbstEventBus
import at.yawk.wm.hl.Monitor
import at.yawk.wm.hl.Tag
import at.yawk.wm.ui.Direction
import at.yawk.wm.ui.FlowCompositeWidget
import at.yawk.wm.ui.RenderElf
import at.yawk.wm.ui.TextWidget
import java.util.ArrayList
import javax.inject.Inject

@DockWidget(position = DockWidget.Position.LEFT)
class TagListWidget @Inject constructor(
    private val fontSource: FontSource,
    private val herbstClient: HerbstClient,
    private val renderElf: RenderElf,
    private val herbstEventBus: HerbstEventBus,
    private val monitor: Monitor,
) : FlowCompositeWidget() {

    private val tagWidgets: MutableList<TextWidget> = ArrayList()
    override fun init() {
        update()
        herbstEventBus.addTagEventHandler { update() }
    }

    private fun update() {
        var i = 0
        val tags = herbstClient.getTags(monitor)
        while (i < tags.size) {
            val tag = tags[i]
            var widget: TextWidget
            if (tagWidgets.size > i) {
                widget = tagWidgets[i]
            } else {
                widget = TextWidget()
                if (tagWidgets.isEmpty()) {
                    widget.after(anchor, Direction.HORIZONTAL)
                } else {
                    widget.after(tagWidgets[tagWidgets.size - 1], Direction.HORIZONTAL)
                }
                addWidget(widget)
                tagWidgets.add(widget)
            }
            val style = when (tag.state) {
                Tag.State.SELECTED -> DockConfig.activeFont
                Tag.State.SELECTED_ELSEWHERE -> DockConfig.activeElsewhereFont
                Tag.State.RUNNING -> DockConfig.runningFont
                Tag.State.EMPTY -> DockConfig.emptyFont
                else -> DockConfig.runningFont
            }
            widget.font = fontSource.getFont(style)
            widget.text = tag.id
            i++
        }
        renderElf.render()
    }
}