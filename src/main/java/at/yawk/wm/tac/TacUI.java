package at.yawk.wm.tac;

import at.yawk.wm.x.*;
import at.yawk.wm.x.Graphics;
import at.yawk.wm.x.Window;
import at.yawk.wm.x.event.ExposeEvent;
import at.yawk.wm.x.event.FocusLostEvent;
import at.yawk.wm.x.event.KeyPressEvent;
import at.yawk.wm.x.font.FontCache;
import at.yawk.wm.x.font.GlyphFont;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

/**
 * @author yawkat
 */
public class TacUI extends AbstractResource implements Modal {
    private static final int ENTRY_LIMIT = 20;

    private final TacConfig config;
    private final XcbConnector connector;
    private final int x;
    private final int y;
    private final int width;

    private Window window;
    private Graphics graphics;

    private List<EntryState> lastEntries = Collections.emptyList();
    @Getter
    private List<Entry> entries = Collections.emptyList();

    private final GlyphFont primaryNormal;
    private final GlyphFont secondaryNormal;
    private final GlyphFont primarySelected;
    private final GlyphFont secondarySelected;

    private final List<Feature> features = new ArrayList<>();

    public TacUI(TacConfig config, FontCache fontCache, XcbConnector connector, int x, int y) {
        this.config = config;
        this.connector = connector;
        this.width = config.getWidth();
        this.x = x;
        this.y = y;

        primaryNormal = fontCache.getFont(this.config.getFontPrimary());
        secondaryNormal = fontCache.getFont(this.config.getFontSecondary());
        primarySelected = fontCache.getFont(this.config.getFontPrimarySelected());
        secondarySelected = fontCache.getFont(this.config.getFontSecondarySelected());

        addFeature(new CloseFeature());
    }

    public void setEntries(Stream<? extends Entry> entries) {
        Stream<? extends Entry> stream = entries;
        for (Feature feature : features) {
            stream = feature.setEntries(stream, ENTRY_LIMIT);
        }
        this.entries = stream.limit(ENTRY_LIMIT).collect(Collectors.toList());
        features.forEach(Feature::onEntriesSet);
        render(false);
    }

    private synchronized void render(boolean expose) {
        int newHeight = entries.size() * config.getRowHeight();

        if (window == null) {
            window = connector.getScreen().createWindow(
                    EventGroup.PAINT, EventGroup.FOCUS, EventGroup.KEYBOARD
            );
            window.addListener(ExposeEvent.class, evt -> {
                window.acquireFocus();
                render(true);
            });
            window.addListener(KeyPressEvent.class, evt -> {
                for (Feature feature : features) {
                    feature.onKeyPress(evt);
                    if (evt.isCancelled()) { break; }
                }
            });
            long startTime = System.currentTimeMillis();
            window.addListener(FocusLostEvent.class, evt -> {
                // HACK: sometimes a focus lost event would be received immediately after expose,
                // closing the UI
                if (startTime >= System.currentTimeMillis() - 100) {
                    return;
                }

                close();
            });
            graphics = window.createGraphics();
            window.setBackgroundColor(config.getColorBackground())
                    .setBounds(x, y, width, newHeight)
                    .setDock()
                    .show();
            return; // wait for expose
        }

        int lastHeight = lastEntries.size() * config.getRowHeight();

        if (lastHeight != newHeight) {
            window.setBounds(x, y, width, newHeight);
        }

        for (int i = 0; i < entries.size(); i++) {
            EntryState entry = entries.get(i).state;
            if (!expose && lastEntries.size() > i && lastEntries.get(i).equals(entry)) {
                // can skip that render
                continue;
            }
            int y = i * config.getRowHeight();
            Color background = entry.isSelected() ? config.getColorSelected() : config.getColorBackground();
            graphics.setForegroundColor(background);
            graphics.fillRect(0, y, width, config.getRowHeight());
            GlyphFont font;
            if (entry.isLowPriority()) {
                font = entry.isSelected() ? secondarySelected : secondaryNormal;
            } else {
                font = entry.isSelected() ? primarySelected : primaryNormal;
            }
            graphics.setFont(font);
            int h = font.getStringBounds(entry.getText()).height;
            graphics.drawText(config.getPadding(), y + (config.getRowHeight() - h) / 2, entry.getText());
        }
        graphics.flush();
        lastEntries = entries.stream().map(e -> e.state).collect(Collectors.toList());
    }

    public void update() {
        render(false);
    }

    @Override
    public void close() {
        if (window != null) {
            window.close();
            window = null;
            closeListeners.forEach(Runnable::run);
        }
    }

    private List<Runnable> closeListeners = new ArrayList<>();

    @Override
    public void addCloseListener(Runnable listener) {
        closeListeners.add(listener);
    }

    public void addFeature(Feature feature) {
        feature.onAdd(this);
        features.add(feature);
    }
}
