package at.yawk.wm.style;

import java.nio.file.Path;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class StyleConfig {
    private Map<FontDescriptor, FontStyle> fonts;
    private Path fontCacheDir;
}
