package at.yawk.wm.dock;

import at.yawk.wm.style.FontStyle;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.icon.Icon;
import java.awt.*;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public class IconWidget extends Widget {
    @Nullable private Icon icon;
    private Color foreground;
    private Color background;
    private int targetHeight = -1;

    public IconWidget() {}

    public IconWidget(@Nullable Icon icon) {
        this.icon = icon;
    }

    public void setColor(FontStyle style) {
        setForeground(style.getForeground());
        setBackground(style.getBackground());
    }

    @Override
    protected void layout(Graphics graphics) {
        setWidth(icon != null ? icon.getWidth() : 0);
        if (targetHeight != -1) {
            setHeight(targetHeight);
        } else {
            setHeight(icon != null ? icon.getHeight() : 0);
        }
    }

    @Override
    protected void render(Graphics graphics) {
        if (icon != null) {
            int x = Math.min(getX(), getX2());
            int y = Math.min(getY(), getY2());
            if (targetHeight != -1) {
                // center vertically
                y += (targetHeight - icon.getHeight()) / 2;
            }
            graphics.drawPixMap(icon.colorize(foreground, background), x, y);
        }
    }

    public void setIcon(@Nullable Icon icon) {
        if (!Objects.equals(this.icon, icon)) {
            this.icon = icon;
            markDirty();
        }
    }

    public void setForeground(Color foreground) {
        if (!Objects.equals(this.foreground, foreground)) {
            this.foreground = foreground;
            markDirty();
        }
    }

    public void setBackground(Color background) {
        if (!Objects.equals(this.background, background)) {
            this.background = background;
            markDirty();
        }
    }

    public void setTargetHeight(int targetHeight) {
        if (this.targetHeight != targetHeight) {
            this.targetHeight = targetHeight;
            markDirty();
        }
    }
}
