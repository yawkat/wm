package at.yawk.wm.style;

import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class StandardFontFactory implements FontFactory {
    private static final int DEFAULT_SIZE = 12;

    private String name = Font.MONOSPACED;

    @Override
    public int getCellSize(FontStyle style) {
        return (int) (style.getSize() * 1.5); // better too large than too small
    }

    @Override
    public String getDescriptor() {
        return name;
    }

    @Override
    public Font createFont(FontStyle style) {
        int flags = 0;
        if (style.getBold()) { flags |= Font.BOLD; }
        if (style.getItalic()) { flags |= Font.ITALIC; }
        return new Font(name, flags, style.getSize());
    }
}
