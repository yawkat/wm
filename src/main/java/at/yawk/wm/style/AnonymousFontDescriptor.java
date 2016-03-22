package at.yawk.wm.style;

/**
 * @author yawkat
 */
class AnonymousFontDescriptor implements FontDescriptor {
    private final FontStyle style;

    @java.beans.ConstructorProperties({ "style" })
    public AnonymousFontDescriptor(FontStyle style) {
        this.style = style;
    }

    public FontStyle getStyle() {
        return this.style;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof AnonymousFontDescriptor)) { return false; }
        final AnonymousFontDescriptor other = (AnonymousFontDescriptor) o;
        final Object this$style = this.style;
        final Object other$style = other.style;
        if (this$style == null ? other$style != null : !this$style.equals(other$style)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $style = this.style;
        result = result * PRIME + ($style == null ? 0 : $style.hashCode());
        return result;
    }

    public String toString() {
        return "at.yawk.wm.style.AnonymousFontDescriptor(style=" + this.style + ")";
    }
}
