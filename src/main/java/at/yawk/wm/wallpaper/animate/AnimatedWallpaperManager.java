package at.yawk.wm.wallpaper.animate;

import at.yawk.wm.Scheduler;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.image.LocalImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Future;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Singleton
@Slf4j
public class AnimatedWallpaperManager {
    @Inject XcbConnector connector;
    @Inject Scheduler scheduler;
    @Inject AnimatedWallpaperConfig wallpaperConfig;

    private Animator animator;

    @SneakyThrows
    public void start() {
        log.info("Initializing wallpaper...");

        AnimatedWallpaper wallpaper = null;

        if (Files.exists(wallpaperConfig.getCache())) {
            FileTime cacheMod = Files.getLastModifiedTime(wallpaperConfig.getCache());
            FileTime inMod = Files.getLastModifiedTime(wallpaperConfig.getInput());
            if (cacheMod.compareTo(inMod) >= 0) {
                log.info("Found valid cached wallpaper animation");
                try (DataInputStream in = new DataInputStream(Files.newInputStream(wallpaperConfig.getCache()))) {
                    wallpaper = AnimatedWallpaper.read(in);
                }
                log.info("Wallpaper loaded to memory");
            }
        }

        if (wallpaper == null) {
            log.info("Need to compile the wallpaper animation, this may take a while!");
            wallpaper = AnimationBuilder.loadDirectory(wallpaperConfig.getInput());
            try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(wallpaperConfig.getCache()))) {
                wallpaper.write(out);
            }
            log.info("Compilation complete");
        }

        show(wallpaper);
    }

    private void show(AnimatedWallpaper wallpaper) {
        Window rootWindow = connector.getScreen().getRootWindow();
        animator = new Animator(wallpaper, wallpaperConfig.getBackgroundColor(), scheduler, rootWindow);
        animator.start();
    }

    public void drawImage(LocalImage image, int x, int y) {
        animator.drawImage(image, x, y);
    }

    public Future<?> stop() {
        return animator.stop();
    }
}
