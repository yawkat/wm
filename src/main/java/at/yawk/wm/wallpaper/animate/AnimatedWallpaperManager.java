package at.yawk.wm.wallpaper.animate;

import at.yawk.wm.Config;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@Component
public class AnimatedWallpaperManager {
    @Inject XcbConnector connector;
    @Inject ScheduledExecutorService executor;
    @Inject Config config;

    private Animator animator;

    @PostConstruct
    @SneakyThrows
    void start() {
        AnimatedWallpaper wallpaper = null;

        AnimatedWallpaperConfig wallpaperConfig = config.getWallpaper();
        if (Files.exists(wallpaperConfig.getCache())) {
            FileTime cacheMod = Files.getLastModifiedTime(wallpaperConfig.getCache());
            FileTime inMod = Files.getLastModifiedTime(wallpaperConfig.getInput());
            if (cacheMod.compareTo(inMod) >= 0) {
                try (DataInputStream in = new DataInputStream(Files.newInputStream(wallpaperConfig.getCache()))) {
                    wallpaper = AnimatedWallpaper.read(in);
                }
            }
        }

        if (wallpaper == null) {
            wallpaper = AnimationBuilder.loadDirectory(wallpaperConfig.getInput());
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(wallpaperConfig.getCache()))) {
                wallpaper.write(out);
            }
        }

        show(wallpaper);
    }

    private void show(AnimatedWallpaper wallpaper) {
        Window rootWindow = connector.getScreen().getRootWindow();
        animator = new Animator(wallpaper, config.getWallpaper().getBackgroundColor(), executor, rootWindow);
        animator.start();
    }

    public void stop() {
        animator.stop();
    }
}
