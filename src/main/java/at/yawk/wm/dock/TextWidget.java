package at.yawk.wm.dock;

import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import java.util.Objects;
import lombok.AccessLevel;
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

    @Getter(AccessLevel.NONE)
    private String layoutText;
    @Getter(AccessLevel.NONE)
    private Dimension layoutTextBounds;

    @Override
    protected void layout(Graphics graphics) {
        layoutText = text;

        layoutTextBounds = font.getStringBounds(text);
        setWidth(layoutTextBounds.width + padding * 2);
        if (textHeight == 0) {
            setHeight((int) layoutTextBounds.getHeight());
        } else {
            setHeight(textHeight);
        }
    }

    @Override
    protected void render(Graphics graphics) {
        // local copy
        String text = this.text;

        int x = Math.min(getX(), getX2()) + padding;
        int y = Math.min(getY(), getY2());
        if (textHeight != 0) {
            // center text vertically
            y += (textHeight - layoutTextBounds.height) / 2;
        }
        graphics.setFont(font);
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
