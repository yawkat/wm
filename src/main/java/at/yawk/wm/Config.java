package at.yawk.wm;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.tac.TacConfig;
import at.yawk.wm.x.font.FontFactory;
import java.nio.file.Path;
import java.util.Map;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
public class Config {
    private DockConfig dock;
    private TacConfig tac;
    private Path fontCacheDir;
    private FontFactory font;
    private Map<String, String> shortcuts;
}
