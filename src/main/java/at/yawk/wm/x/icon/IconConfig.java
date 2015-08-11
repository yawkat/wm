package at.yawk.wm.x.icon;

import java.nio.file.Path;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class IconConfig {
    private Path cacheDir;
    private Map<IconDescriptor, Path> icons;
}
