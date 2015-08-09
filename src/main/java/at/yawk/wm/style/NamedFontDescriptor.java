package at.yawk.wm.style;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class NamedFontDescriptor implements FontDescriptor {
    final String id;

    @JsonCreator
    public NamedFontDescriptor(String id) {
        this.id = id;
    }
}
