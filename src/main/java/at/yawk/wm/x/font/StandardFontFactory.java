package at.yawk.wm.x.font;

import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class StandardFontFactory implements FontFactory {
    private static final int DEFAULT_SIZE = 12;

    private String name = Font.MONOSPACED;
    private int size = DEFAULT_SIZE;

    @Override
    public int getCellSize() {
        return (int) (size * 1.5); // better too large than too small
    }

    @Override
    public String getDescriptor() {
        StringBuilder builder = new StringBuilder(name);
        if (size != DEFAULT_SIZE) { builder.append("-size").append(size); }
        return builder.toString();
    }

    @Override
    public Font createFont(FontStyle style) {
        int flags = 0;
        if (style.isBold()) { flags |= Font.BOLD; }
        if (style.isItalic()) { flags |= Font.ITALIC; }
        return new Font(name, flags, style.getSize());
    }
}
