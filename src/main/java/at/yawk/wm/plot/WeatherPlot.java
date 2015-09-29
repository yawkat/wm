package at.yawk.wm.plot;

import at.yawk.wm.Scheduler;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager;
import at.yawk.wm.x.image.BufferedLocalImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.yarn.Component;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
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
public class WeatherPlot {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Inject AnimatedWallpaperManager wallpaperManager;
    @Inject ObjectMapper objectMapper;
    @Inject DockConfig dockConfig;
    @Inject WeatherConfig config;

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
        wallpaperManager.drawImage(image, 0, dockConfig.getHeight());
    }

    @VisibleForTesting
    BufferedImage pollImage() throws IOException, InterruptedException {
        Forecast forecast;
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + config.getLocation() + "&mode=json";
        try (InputStream in = new URL(url).openStream()) {
            forecast = objectMapper.readValue(in, Forecast.class);
        }
        Instant start = Instant.now();
        // three days max
        Instant deadline = start.plus(60 * 60 * 24 * 3, ChronoUnit.SECONDS);

        List<ForecastEntry> entries = forecast.getEntries();
        entries.removeIf(e -> e.getTime().isAfter(deadline));
        entries.sort(Comparator.comparing(ForecastEntry::getTime));

        Object[][] table = entries.stream()
                .map(e -> {
                    double mm = e.getRain() == null ?
                            0 :
                            e.getRain().getThreeHour();
                    return new Object[]{ format(e.getTime()), mm };
                })
                .toArray(Object[][]::new);

        return new PlotBuilder()
                .command(WeatherPlot.class.getResource("weather.gp"))
                .parameter("start0Day", getDateTime(start).withHour(0).withMinute(0).withSecond(0).format(DATE_FORMAT))
                .parameter("start", format(start))
                .parameter("end", format(entries.get(entries.size() - 1).getTime()))
                .parameter("rain", config.getRainColor())
                .parameter("grid", config.getMarkColor())
                .dataArray(table)
                .plot(config.getBackgroundColor(), 400, 300);

    }

    private String format(Instant time) {
        return getDateTime(time).format(DATE_FORMAT);
    }

    private OffsetDateTime getDateTime(Instant instant) {
        return instant.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Forecast {
        @JsonProperty("list")
        private List<ForecastEntry> entries;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ForecastEntry {
        @JsonProperty("dt")
        private Instant time;
        private DownfallMetric rain;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class DownfallMetric {
        @JsonProperty("3h")
        private Double threeHour;
    }

    @Data
    public static class WeatherConfig {
        private String location;
        private Color markColor;
        private Color rainColor;
        private Color backgroundColor;
    }
}
