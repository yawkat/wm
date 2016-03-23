package at.yawk.wm.hl

/**
 * @author yawkat
 */
data class TitleEvent(val title: String) { // todo: monitor
    interface Handler {
        fun handle(event: TitleEvent)
    }
}
