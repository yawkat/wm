package at.yawk.wm.x.icon;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class IconDescriptor {
    final String id;

    @JsonCreator
    public IconDescriptor(String id) {
        this.id = id;
    }
}
