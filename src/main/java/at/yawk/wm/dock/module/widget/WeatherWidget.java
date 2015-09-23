package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.Widget;
import at.yawk.wm.dock.module.DockConfig;
import at.yawk.wm.dock.module.DockWidget;
import at.yawk.wm.dock.module.Periodic;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.PixMap;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.ZFormatImage;
import at.yawk.wm.x.image.LocalImage;
import at.yawk.yarn.Component;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.msgpack.core.annotations.VisibleForTesting;

/**
 * @author yawkat
 */
@Slf4j
@Component
@DockWidget(position = DockWidget.Position.RIGHT, priority = 300)
public class WeatherWidget extends Widget {
    @Inject XcbConnector connector;
    @Inject ObjectMapper objectMapper;
    @Inject DockConfig dockConfig;

    @Nullable
    private volatile PixMap pixMap;

    @Periodic(value = 15, unit = TimeUnit.MINUTES, render = true)
    void update() throws IOException {
        log.info("Getting weather");
        LocalImage image = fetchAndPaint();

        PixMap pixMap = connector.getScreen().getRootWindow().createPixMap(image.getWidth(), image.getHeight());
        at.yawk.wm.x.Graphics graphics = pixMap.createGraphics();
        graphics.putImage(0, 0, image);
        graphics.flush();
        graphics.close();

        PixMap oldMap;
        synchronized (this) {
            oldMap = this.pixMap;
            this.pixMap = pixMap;
        }
        if (oldMap != null) {
            oldMap.close();
        }
    }

    @Override
    protected void layout(Graphics graphics) {
        PixMap pixMap = this.pixMap;
        if (pixMap != null) {
            setWidth(pixMap.getWidth());
            setHeight(pixMap.getHeight());
        }
    }

    @Override
    protected void render(Graphics graphics) {
        super.render(graphics);
        PixMap pixMap = this.pixMap;
        if (pixMap != null) {
            int x = Math.min(getX(), getX2());
            int y = Math.min(getY(), getY2());
            graphics.drawPixMap(pixMap, x, y);
        }
    }

    @VisibleForTesting
    LocalImage fetchAndPaint() throws IOException {
        WeatherConfig config = dockConfig.getWeather();

        Forecast forecast;
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + config.getLocation() + "&mode=json";
        try (InputStream in = new URL(url).openStream()) {
            forecast = objectMapper.readValue(in, Forecast.class);
        }

        List<ForecastEntry> entries = forecast.getEntries();
        entries.removeIf(e -> e.getRain() == null || e.getRain().getThreeHour() == null);
        entries.sort(Comparator.comparing(ForecastEntry::getTime));

        int height = dockConfig.getHeight();
        int xPerHour = 2;
        OffsetDateTime minTime = getDateTime(entries.get(0).getTime());
        OffsetDateTime maxTime = getDateTime(entries.get(entries.size() - 1).getTime());
        int width = round((hoursDifference(minTime, maxTime) + 1) * xPerHour);

        int bg = dockConfig.getBackground().getRGB();
        LocalImage image = ZFormatImage.TYPE.createImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRgb(x, y, bg);
            }
        }

        // draw marks
        int markInterval = 6;
        OffsetDateTime mark = minTime.withSecond(0).withMinute(0);
        // round down to next multiple of markInterval
        mark = mark.withHour(mark.getHour() / markInterval * markInterval);
        while (mark.isBefore(maxTime)) {
            if (!mark.isBefore(minTime)) {
                int x = round(hoursDifference(minTime, mark) * xPerHour);
                boolean markDotted = mark.getHour() != 0;
                for (int y = 0; y < height; y++) {
                    if (markDotted) {
                        int i = y % 4;
                        if (i < 1 || i > 2) {
                            continue;
                        }
                    }

                    image.setRgb(x, y, config.getMarkColor().getRGB());
                }
            }

            mark = mark.plus(markInterval, ChronoUnit.HOURS);
        }

        int prevX = 0;
        int nextX = 0;
        for (int i = 0; i < entries.size(); i++) {
            ForecastEntry entry = entries.get(i);
            int cx = nextX;
            nextX = i == entries.size() - 1 ? width :
                    round(hoursDifference(minTime, getDateTime(entries.get(i + 1).getTime())) * xPerHour);
            int y = height - round(entry.getRain().getThreeHour() * height);
            if (y > height) { y = height; }
            if (y < 0) { y = 0; }

            // draw bar
            int start = (prevX + cx) / 2;
            int end = (nextX + cx) / 2;
            for (int x = start; x < end; x++) {
                for (int oy = y; oy < height; oy++) {
                    image.setRgb(x, oy, config.getRainColor().getRGB());
                }
            }
            prevX = cx;
        }

        return image;
    }

    private static int round(double d) {
        return Math.toIntExact(Math.round(d));
    }

    private static double hoursDifference(Temporal from, Temporal to) {
        return from.until(to, ChronoUnit.MINUTES) / 60D;
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
    }
}
