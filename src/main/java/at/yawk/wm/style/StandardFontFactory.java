package at.yawk.wm.style;

import java.awt.*;

/**
 * @author yawkat
 */
public class StandardFontFactory implements FontFactory {
    private static final int DEFAULT_SIZE = 12;

    private String name = Font.MONOSPACED;

    public StandardFontFactory() {
    }

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

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof StandardFontFactory)) { return false; }
        final StandardFontFactory other = (StandardFontFactory) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$name = this.name;
        final Object other$name = other.name;
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $name = this.name;
        result = result * PRIME + ($name == null ? 0 : $name.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof StandardFontFactory;
    }

    public String toString() {
        return "at.yawk.wm.style.StandardFontFactory(name=" + this.name + ")";
    }
}
