package at.yawk.wm.tac.password

import at.yawk.wm.x.font.AwtGlyphFileFactory
import java.awt.BorderLayout
import java.awt.Dialog
import java.awt.Dimension
import java.awt.Frame
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Window
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog
import javax.swing.JTextArea

internal open class TextEditorWindow {
    val window: Window
    private val field: JTextArea

    val text: String
        get() = this.field.text

    constructor(text: String) {
        this.field.text = text
    }

    fun show() {
        window.isVisible = true
    }

    open fun close() {
        field.text = "" // clear text
        window.dispose()
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
        val size = Dimension(PasswordConfig.editorWidth, PasswordConfig.editorHeight)
        field = object : JTextArea() {
            override fun paintComponent(g: Graphics) {
                if (g is Graphics2D) {
                    g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR
                    )
                }
                super.paintComponent(g)
            }
        }
        field.setText(text)
        field.setBackground(PasswordConfig.editorBackground.awt)
        val style = PasswordConfig.editorFont
        field.setFont(AwtGlyphFileFactory.getFont(style))
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
}