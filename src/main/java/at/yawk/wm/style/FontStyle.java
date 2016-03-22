package at.yawk.wm.style;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.*;
import javax.annotation.Nullable;

/**
 * @author yawkat
 */
public class FontStyle {
    @Nullable private FontDescriptor parent = null;

    private FontFactory family;
    private Color foreground;
    private Color background;
    private Boolean bold;
    private Boolean italic;
    private Integer size;

    public FontStyle() {
    }

    void validate(FontDescriptor descriptor) {
        if (family == null) { throw new RuntimeException("No family on " + descriptor); }
        if (foreground == null) { throw new RuntimeException("No foreground on " + descriptor); }
        if (background == null) { throw new RuntimeException("No background on " + descriptor); }
        if (bold == null) { throw new RuntimeException("No bold on " + descriptor); }
        if (italic == null) { throw new RuntimeException("No italic on " + descriptor); }
        if (size == null) { throw new RuntimeException("No size on " + descriptor); }
    }

    @JsonIgnore
    public String getDescriptor() {
        StringBuilder builder = new StringBuilder(family.getDescriptor());
        builder.append('-').append(String.format("%06x", foreground.getRGB()));
        builder.append('-').append(String.format("%06x", background.getRGB()));
        if (bold) { builder.append("-bold"); }
        if (italic) { builder.append("-italic"); }
        builder.append('-').append(size);
        return builder.toString();
    }

    public FontStyle withColor(Color color) {
        if (color.equals(this.foreground)) { return this; }
        FontStyle fs = new FontStyle();
        fs.foreground = color;
        fs.family = family;
        fs.background = background;
        fs.bold = bold;
        fs.italic = italic;
        fs.size = size;
        return fs;
    }

    @Nullable
    public FontDescriptor getParent() {
        return this.parent;
    }

    public FontFactory getFamily() {
        return this.family;
    }

    public Color getForeground() {
        return this.foreground;
    }

    public Color getBackground() {
        return this.background;
    }

    public Boolean getBold() {
        return this.bold;
    }

    public Boolean getItalic() {
        return this.italic;
    }

    public Integer getSize() {
        return this.size;
    }

    public void setParent(@Nullable FontDescriptor parent) {
        this.parent = parent;
    }

    public void setFamily(FontFactory family) {
        this.family = family;
    }

    public void setForeground(Color foreground) {
        this.foreground = foreground;
    }

    public void setBackground(Color background) {
        this.background = background;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public boolean equals(Object o) {
        if (o == this) { return true; }
        if (!(o instanceof FontStyle)) { return false; }
        final FontStyle other = (FontStyle) o;
        if (!other.canEqual((Object) this)) { return false; }
        final Object this$parent = this.parent;
        final Object other$parent = other.parent;
        if (this$parent == null ? other$parent != null : !this$parent.equals(other$parent)) { return false; }
        final Object this$family = this.family;
        final Object other$family = other.family;
        if (this$family == null ? other$family != null : !this$family.equals(other$family)) { return false; }
        final Object this$foreground = this.foreground;
        final Object other$foreground = other.foreground;
        if (this$foreground == null ? other$foreground != null : !this$foreground.equals(other$foreground)) {
            return false;
        }
        final Object this$background = this.background;
        final Object other$background = other.background;
        if (this$background == null ? other$background != null : !this$background.equals(other$background)) {
            return false;
        }
        final Object this$bold = this.bold;
        final Object other$bold = other.bold;
        if (this$bold == null ? other$bold != null : !this$bold.equals(other$bold)) { return false; }
        final Object this$italic = this.italic;
        final Object other$italic = other.italic;
        if (this$italic == null ? other$italic != null : !this$italic.equals(other$italic)) { return false; }
        final Object this$size = this.size;
        final Object other$size = other.size;
        if (this$size == null ? other$size != null : !this$size.equals(other$size)) { return false; }
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $parent = this.parent;
        result = result * PRIME + ($parent == null ? 0 : $parent.hashCode());
        final Object $family = this.family;
        result = result * PRIME + ($family == null ? 0 : $family.hashCode());
        final Object $foreground = this.foreground;
        result = result * PRIME + ($foreground == null ? 0 : $foreground.hashCode());
        final Object $background = this.background;
        result = result * PRIME + ($background == null ? 0 : $background.hashCode());
        final Object $bold = this.bold;
        result = result * PRIME + ($bold == null ? 0 : $bold.hashCode());
        final Object $italic = this.italic;
        result = result * PRIME + ($italic == null ? 0 : $italic.hashCode());
        final Object $size = this.size;
        result = result * PRIME + ($size == null ? 0 : $size.hashCode());
        return result;
    }

    protected boolean canEqual(Object other) {
        return other instanceof FontStyle;
    }

    public String toString() {
        return "at.yawk.wm.style.FontStyle(parent=" + this.parent + ", family=" + this.family + ", foreground=" +
               this.foreground + ", background=" + this.background + ", bold=" + this.bold + ", italic=" + this.italic +
               ", size=" + this.size + ")";
    }
}
