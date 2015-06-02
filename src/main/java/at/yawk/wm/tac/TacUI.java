package at.yawk.wm.tac;

import at.yawk.wm.Config;
import at.yawk.wm.x.AbstractResource;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.font.GlyphFont;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yawkat
 */
public class TacUI extends AbstractResource {
    private static final int ENTRY_LIMIT = 20;
    private static final int ROW_HEIGHT = 16;

    private final TacConfig config;
    private final XcbConnector connector;
    private final int x;
    private final int y;
    private final int width;

    private Window window;
    private Graphics graphics;

    private List<EntryState> lastEntries = Collections.emptyList();
    private List<Entry> entries = Collections.emptyList();

    private final TacFontMap fontMap;

    public TacUI(Config config, XcbConnector connector, int x, int y, int width) {
        this.config = config.getTac();
        this.connector = connector;
        this.width = width;
        this.x = x;
        this.y = y;
        fontMap = new TacFontMap(config.getFontCacheDir(), config.getFont());
    }

    public void setEntries(List<Entry> entries) {
        this.entries = new ArrayList<>(entries.subList(0, Math.min(entries.size(), ENTRY_LIMIT)));
        render();
    }

    private synchronized void render() {
        int lastHeight = (lastEntries.size() + 1) * ROW_HEIGHT;
        int newHeight = (entries.size() + 1) * ROW_HEIGHT;

        if (window == null) {
            window = connector.getScreen().createWindow();
            graphics = window.createGraphics();
            lastHeight = 0;
        }
        if (lastHeight != newHeight) {
            window.setBounds(x, y, width, newHeight);
        }

        for (int i = 0; i < entries.size(); i++) {
            EntryState entry = entries.get(i).state;
            if (lastEntries.size() > i && lastEntries.get(i).equals(entry)) {
                // can skip that render
                continue;
            }
            int y = (i + 1) * 16;
            graphics.setForegroundColor(
                    entry.isSelected() ? config.getColorBackground() : config.getColorSelected()
            );
            graphics.fillRect(0, y, width, ROW_HEIGHT);
            GlyphFont font = fontMap.getFont(
                    entry.isLowPriority() ? config.getFontLowPriority() : config.getFontNormal(),
                    entry.isSelected() ? config.getColorSelected() : config.getColorBackground()
            );
            graphics.setFont(font);
            int h = font.getStringBounds(entry.getText()).height;
            graphics.drawText(0, y + (ROW_HEIGHT - h) / 2, entry.getText());
        }
        lastEntries = entries.stream().map(e -> e.state).collect(Collectors.toList());
    }

    @Override
    public void close() {
        if (window != null) { window.close(); }
    }

    public static <T extends String> void m(T obj) {
        Class<? extends String> c = obj.getClass();
    }
}
