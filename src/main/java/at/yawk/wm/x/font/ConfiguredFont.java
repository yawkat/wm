package at.yawk.wm.x.font;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.awt.*;
import lombok.Value;

/**
 * @author yawkat
 */
@Value
public class ConfiguredFont {
    private final FontStyle style;
    private final Color background;
    private final FontFactory font;

    @JsonIgnore
    String getDescriptor() {
        return String.format("%s-%06x-%06x", font.getDescriptor(), style.getColor().getRGB(), background.getRGB());
    }
}
