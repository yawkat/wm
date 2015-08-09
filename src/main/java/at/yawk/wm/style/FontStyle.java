package at.yawk.wm.style;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.*;
import javax.annotation.Nullable;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class FontStyle {
    @Nullable private FontDescriptor parent = null;

    private FontFactory family;
    private Color foreground;
    private Color background;
    private Boolean bold;
    private Boolean italic;
    private Integer size;

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
}
