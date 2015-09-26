package at.yawk.wm.plot;

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
public class TrafficPlotTest {
    @Test
    public void testPollImage() throws Exception {
        TrafficPlot plot = new TrafficPlot();
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