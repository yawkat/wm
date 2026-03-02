package at.yawk.wm.tac.password

import at.yawk.wm.x.font.AwtGlyphFileFactory
import java.awt.BorderLayout
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Frame
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.UIManager

internal open class TextEditorWindow {
    val window: Window
    private val field: JTextArea

    val text: String
        get() = this.field.text

    constructor(text: String) {
        this.field.text = text
    }

    fun show() {
        SwingUtilities.invokeLater { window.isVisible = true }
    }

    open fun close() {
        SwingUtilities.invokeLater {
            field.text = ""
            window.dispose()
        }
    }

    fun setTitle(title: String?) {
        if (window is Frame) {
            window.title = title
        } else if (window is Dialog) {
            window.title = title
        }
    }

    fun getField(): JTextArea {
        return field
    }

    init {
        initSwing()
        val size = Dimension(PasswordConfig.editorWidth, PasswordConfig.editorHeight)
        val style = PasswordConfig.editorFont
        val font = AwtGlyphFileFactory.getFont(style)
        field = JTextArea()
        field.setText(text)
        field.setBackground(PasswordConfig.editorBackground.awt)
        field.setFont(font)
        field.setForeground(style.foreground.awt)
        field.setSelectedTextColor(PasswordConfig.editorBackground.awt)
        field.setSelectionColor(style.foreground.awt)
        field.setCaretColor(style.foreground.awt)
        field.setLineWrap(true)
        window = JDialog()
        window.size = size
        window.setBackground(PasswordConfig.editorBackground.awt)
        window.layout = BorderLayout()
        window.add(field, BorderLayout.CENTER)
        window.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                close()
            }
        })
        window.isLocationByPlatform = true
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(TextEditorWindow::class.java)
        private var swingInitialized = false

        @Synchronized
        private fun initSwing() {
            if (!swingInitialized) {
                swingInitialized = true
                log.info("Initializing Swing: DISPLAY={}", System.getenv("DISPLAY"))
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
                    log.info("Set cross-platform LookAndFeel successfully")
                } catch (e: Exception) {
                    log.warn("Failed to set cross-platform LookAndFeel", e)
                }
            }
        }
    }
}
