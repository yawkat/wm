package at.yawk.wm.x.font;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.*;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class FontStyle {
    private Color color;
    private boolean bold = false;
    private boolean italic = false;
    private int size = 12;

    @JsonIgnore
    String getDescriptor() {
        StringBuilder builder = new StringBuilder(String.format("%06x", color.getRGB()));
        if (bold) { builder.append("-bold"); }
        if (italic) { builder.append("-italic"); }
        builder.append('-').append(size);
        return builder.toString();
    }

    public FontStyle withColor(Color color) {
        if (color.equals(this.color)) { return this; }
        FontStyle fs = new FontStyle();
        fs.color = color;
        fs.bold = bold;
        fs.italic = italic;
        fs.size = size;
        return fs;
    }
}
