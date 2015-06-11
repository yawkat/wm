package at.yawk.wm.tac.password;

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

    public TextEditorWindow(PasswordConfig config, String text) {
        field = new JTextArea();
        field.setText(text);
        field.setBackground(config.getEditorBackground());
        field.setFont(config.getEditorFont().createFont(config.getEditorFontStyle()));
        field.setForeground(config.getEditorFontStyle().getColor());
        field.setSelectedTextColor(config.getEditorBackground());
        field.setSelectionColor(config.getEditorFontStyle().getColor());
        field.setCaretColor(config.getEditorFontStyle().getColor());
        Graphics graphics = field.getGraphics();
        if (graphics instanceof Graphics2D) {
            ((Graphics2D) graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                                     RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
        }

        window = new JDialog();
        window.setSize(config.getEditorWidth(), config.getEditorHeight());
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
