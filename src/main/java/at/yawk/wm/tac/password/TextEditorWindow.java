package at.yawk.wm.tac.password;

import at.yawk.wm.style.FontManager;
import at.yawk.wm.style.FontStyle;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import lombok.Getter;

/**
 * @author yawkat
 */
@Getter
class TextEditorWindow {
    private final Window window;
    private final JTextArea field;

    public TextEditorWindow(FontManager fontManager, PasswordConfig config, String text) {
        Dimension size = new Dimension(config.getEditorWidth(), config.getEditorHeight());

        field = new JTextArea() {
            @Override
            protected void paintComponent(Graphics g) {
                if (g instanceof Graphics2D) {
                    ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                      RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
                }
                super.paintComponent(g);
            }
        };
        field.setText(text);
        field.setBackground(config.getEditorBackground());
        FontStyle style = fontManager.resolve(config.getEditorFont());
        field.setFont(style.getFamily().createFont(style));
        field.setForeground(style.getForeground());
        field.setSelectedTextColor(config.getEditorBackground());
        field.setSelectionColor(style.getForeground());
        field.setCaretColor(style.getForeground());
        field.setLineWrap(true);

        window = new JDialog();
        window.setSize(size);
        window.setBackground(config.getEditorBackground());
        window.setLayout(new BorderLayout());
        window.add(field, BorderLayout.CENTER);
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
    }

    public String getText() {
        return field.getText();
    }

    public void show() {
        window.setVisible(true);
    }

    public void close() {
        field.setText(""); // clear text
        window.dispose();
    }

    public void setTitle(String title) {
        if (window instanceof Frame) {
            ((Frame) window).setTitle(title);
        } else if (window instanceof Dialog) {
            ((Dialog) window).setTitle(title);
        }
    }
}
