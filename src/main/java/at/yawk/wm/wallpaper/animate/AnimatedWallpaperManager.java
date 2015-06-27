package at.yawk.wm.wallpaper.animate;

import at.yawk.wm.Config;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class AnimatedWallpaperManager {
    @Inject XcbConnector connector;
    @Inject ScheduledExecutorService executor;
    @Inject Config config;

    private Animator animator;

    @PostConstruct
    @SneakyThrows
    void start() {
        log.info("Initializing wallpaper...");

        AnimatedWallpaper wallpaper = null;

        AnimatedWallpaperConfig wallpaperConfig = config.getWallpaper();
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
        animator = new Animator(wallpaper, config.getWallpaper().getBackgroundColor(), executor, rootWindow);
        animator.start();
    }

    public Future<?> stop() {
        return animator.stop();
    }
}
