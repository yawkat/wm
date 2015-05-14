package at.yawk.wm;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.x.font.FontFactory;
import java.nio.file.Path;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Config {
    private DockConfig dock;
    private Path cacheDir;
    private FontFactory font;
}
