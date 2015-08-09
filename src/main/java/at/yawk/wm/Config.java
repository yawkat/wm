package at.yawk.wm;

import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.style.StyleConfig;
import at.yawk.wm.tac.TacConfig;
import at.yawk.wm.tac.launcher.LauncherConfig;
import at.yawk.wm.tac.password.PasswordConfig;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperConfig;
import at.yawk.yarn.Provides;
import lombok.Data;

/**
 * @author yawkat
 */
@Data
class Config {
    private StyleConfig style;
    private DockConfig dock;
    private TacConfig tac;
    private LauncherConfig launcher;
    private PasswordConfig password;
    private AnimatedWallpaperConfig wallpaper;
    private at.yawk.paste.client.Config paste;

    @Provides
    public StyleConfig getStyle() {
        return style;
    }

    @Provides
    public DockConfig getDock() {
        return dock;
    }

    @Provides
    public TacConfig getTac() {
        return tac;
    }

    @Provides
    public LauncherConfig getLauncher() {
        return launcher;
    }

    @Provides
    public PasswordConfig getPassword() {
        return password;
    }

    @Provides
    public AnimatedWallpaperConfig getWallpaper() {
        return wallpaper;
    }

    @Provides
    public at.yawk.paste.client.Config getPaste() {
        return paste;
    }
}
