package at.yawk.wm.plot;

import at.yawk.hawo.traffic.TrafficPoint;
import at.yawk.wm.Scheduler;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager;
import at.yawk.wm.x.image.BufferedLocalImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.yarn.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.core.annotations.VisibleForTesting;

/**
 * @author yawkat
 */
@Slf4j
@Component
public class TrafficPlot {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double Y_MULTIPLIER = 1 << 30;

    @Inject AnimatedWallpaperManager wallpaperManager;
    @Inject ObjectMapper objectMapper;
    @Inject DockConfig dockConfig;
    @Inject TrafficConfig  config;

    @Inject
    void initTask(Scheduler scheduler) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                poll();
            } catch (IOException | InterruptedException e) {
                log.info("Failed to plot weather", e);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    private void poll() throws IOException, InterruptedException {
        LocalImage image = new BufferedLocalImage(pollImage());
        wallpaperManager.drawImage(image, 0, dockConfig.getHeight() + 300);
    }

    @VisibleForTesting
    BufferedImage pollImage() throws IOException, InterruptedException {
        List<TrafficPoint> points;
        try (InputStream in = new URL("http://nas:8000/traffic/history/week").openStream()) {
            points = objectMapper.readValue(in, CollectionType.construct(
                    List.class, objectMapper.constructType(TrafficPoint.class)));
        }

        Object[][] table = points.stream()
                .map(p -> new Object[]{
                        getTime(p).format(DATE_FORMAT),
                        p.getUsedTrafficExternal() / Y_MULTIPLIER,
                        p.getUsedTrafficInternal() / Y_MULTIPLIER })
                .toArray(Object[][]::new);

        System.out.println(Arrays.deepToString(table));

        LocalDateTime first = getTime(points.get(0));
        LocalDateTime min = first.with(DayOfWeek.MONDAY)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime max = min.plus(1, ChronoUnit.WEEKS);

        return new PlotBuilder()
                .command(TrafficPlot.class.getResource("traffic.gp"))
                .parameter("internal", config.getInternalColor())
                .parameter("external", config.getExternalColor())
                .parameter("grid", config.getMarkColor())
                .parameter("minMidDay", min.plus(1, ChronoUnit.HALF_DAYS).format(DATE_FORMAT))
                .parameter("min", min.format(DATE_FORMAT))
                .parameter("max", max.format(DATE_FORMAT))
                .dataArray(table)
                .plot(config.getBackgroundColor(), 400, 300);
    }

    private LocalDateTime getTime(TrafficPoint p) {
        return p.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    @Data
    public static class TrafficConfig {
        private Color markColor;
        private Color externalColor;
        private Color internalColor;
        private Color backgroundColor;
    }
}
