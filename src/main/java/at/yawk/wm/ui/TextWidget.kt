package at.yawk.wm.ui;

import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.font.GlyphFont;
import at.yawk.wm.x.icon.Icon;
import java.awt.*;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public class TextWidget extends Widget {
    private String text;
    @Nullable private Icon icon;
    private GlyphFont font;
    private int textHeight = 0;
    private int paddingLeft = 4;
    private int paddingRight = 4;

    public TextWidget() {
        this("");
    }

    public TextWidget(String text) {
        setText(text);
    }

    private String layoutText;
    private Dimension layoutTextBounds;
    private int boxWidth;
    private int boxHeight;

    @Override
    protected void layout(Graphics graphics) {
        layoutText = text;

        layoutTextBounds = font.getStringBounds(text);
        boxWidth = layoutTextBounds.width;
        if (icon != null) {
            boxWidth += icon.getWidth();
        }

        if (textHeight == 0) {
            boxHeight = (int) layoutTextBounds.getHeight();
        } else {
            boxHeight = textHeight;
        }

        setWidth(boxWidth + paddingLeft + paddingRight);
        setHeight(boxHeight);
    }

    @Override
    protected void render(Graphics graphics) {
        // local copy
        String text = this.text;

        int x0 = Math.min(getX(), getX2());
        int x = x0 + paddingLeft;
        int y = Math.min(getY(), getY2());

        if (textHeight != 0) {
            // center text vertically
            y += (textHeight - layoutTextBounds.height) / 2;
        }

        if (paddingLeft > 0 || paddingRight > 0) {
            graphics.setForegroundColor(font.getStyle().getBackground());
            graphics.fillRect(x0, y, boxWidth + paddingLeft + paddingRight, boxHeight);
        }

        int textStartX = x;
        if (icon != null) {
            graphics.drawPixMap(icon.colorize(font.getStyle().getForeground(), font.getStyle().getBackground()), x, y);
            textStartX += icon.getWidth();
        }
        graphics.setFont(font);
        graphics.drawText(textStartX, y, text);
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

    public void setPaddingLeft(int paddingLeft) {
        if (this.paddingLeft != paddingLeft) {
            this.paddingLeft = paddingLeft;
            markDirty();
        }
    }

    public void setPaddingRight(int paddingRight) {
        if (this.paddingRight != paddingRight) {
            this.paddingRight = paddingRight;
            markDirty();
        }
    }

    public void setIcon(@Nullable Icon icon) {
        if (!Objects.equals(this.icon, icon)) {
            this.icon = icon;
            markDirty();
        }
    }

    public String getText() {
        return this.text;
    }

    @Nullable
    public Icon getIcon() {
        return this.icon;
    }

    public GlyphFont getFont() {
        return this.font;
    }

    public int getTextHeight() {
        return this.textHeight;
    }

    public int getPaddingLeft() {
        return this.paddingLeft;
    }

    public int getPaddingRight() {
        return this.paddingRight;
    }
}
