package at.yawk.wm;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.style.FontDescriptor;
import at.yawk.wm.style.FontStyle;
import at.yawk.wm.tac.TacConfig;
import at.yawk.wm.tac.password.PasswordConfig;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig;
import com.fasterxml.jackson.databind.JsonNode;
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
    private Map<String, JsonNode> shortcuts;
    private PasswordConfig password;
    private AnimatedWallpaperConfig wallpaper;
    private String[] shutdownCommand;
    private at.yawk.paste.client.Config paste;
    private Map<FontDescriptor, FontStyle> fonts;
}
