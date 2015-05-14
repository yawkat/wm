package at.yawk.wm.dock;

import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import java.util.Objects;
import lombok.Getter;

/**
 * @author yawkat
 */
@Getter
public class TextWidget extends Widget {
    private String text;
    private GlyphFont font;
    private int textHeight = 0;
    private int padding = 4;

    public TextWidget() {
        this("");
    }

    public TextWidget(String text) {
        setText(text);
    }

    @Override
    protected void render(Graphics graphics) {
        System.out.println("Rendering " + text + " " + getX() + " " + getY());

        // local copy
        String text = this.text;

        graphics.setFont(font);
        Dimension bounds = font.getStringBounds(text);
        setWidth((int) bounds.getWidth() + padding * 2);
        if (textHeight == 0) {
            setHeight((int) bounds.getHeight());
        } else {
            setHeight(textHeight);
        }
        int x = Math.min(getX(), getX2()) + padding;
        int y = Math.min(getY(), getY2());
        if (textHeight != 0) {
            // center text vertically
            y += (textHeight - bounds.getHeight()) / 2;
        }
        graphics.drawText(x, y, text);
    }

    public void setText(String text) {
        if (!Objects.equals(this.text, text)) {
            this.text = text;
            markDirty();
        }
    }

    public void setFont(GlyphFont font) {
        if (!Objects.equals(this.font, font)) {
            this.font = font;
            markDirty();
        }
    }

    public void setTextHeight(int textHeight) {
        if (this.textHeight != textHeight) {
            this.textHeight = textHeight;
            markDirty();
        }
    }

    public void setPadding(int padding) {
        if (this.padding != padding) {
            this.padding = padding;
            markDirty();
        }
    }
}
