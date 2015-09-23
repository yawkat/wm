package at.yawk.wm.dock.module.widget;

import at.yawk.wm.dock.module.DockConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.io.IOException;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author yawkat
 */
public class WeatherWidgetTest {
    @Test
    public void testFetch() throws IOException {
        WeatherWidget widget = new WeatherWidget();
        widget.objectMapper = new ObjectMapper().findAndRegisterModules();
        widget.dockConfig = new DockConfig();
        widget.dockConfig.setHeight(20);
        widget.dockConfig.setBackground(Color.BLACK);
        widget.fetchAndPaint();
    }
}