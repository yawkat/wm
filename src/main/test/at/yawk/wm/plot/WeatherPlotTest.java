package at.yawk.wm.plot;

import at.yawk.wm.dock.module.DockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.testng.annotations.Test;

/**
 * @author yawkat
 */
public class WeatherPlotTest {
    @Test
    public void testPollImage() throws Exception {
        WeatherPlot plot = new WeatherPlot();
        plot.config = new WeatherPlot.WeatherConfig();
        plot.config.setLocation("Erlangen,de");
        plot.config.setMarkColor(Color.GRAY);
        plot.config.setRainColor(Color.WHITE);
        plot.config.setBackgroundColor(Color.DARK_GRAY);
        plot.objectMapper = new ObjectMapper().findAndRegisterModules();

        open(plot.pollImage());
    }

    private static void open(BufferedImage image) throws IOException, InterruptedException {
        Path path = Files.createTempFile(null, ".png");
        try {
            ImageIO.write(image, "PNG", path.toFile());

            Runtime.getRuntime().exec(new String[]{ "eog", "--", path.toString() }).waitFor();
        } finally {
            Files.delete(path);
        }
    }
}