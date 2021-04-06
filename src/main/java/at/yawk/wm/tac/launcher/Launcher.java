package at.yawk.wm.tac.launcher;

import at.yawk.wm.Util;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.*;
import at.yawk.wm.wallpaper.animate.AnimatedWallpaperManager;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.font.FontCache;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;

@Singleton
public class Launcher {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Launcher.class);
    private final XcbConnector connector;
    private final ModalRegistry modalRegistry;
    private final AnimatedWallpaperManager animatedWallpaper;
    private final ApplicationRunner applicationRunner;
    private final FontCache fontCache;
    private final HerbstClient herbstClient;

    private final PathScanner pathScanner = new PathScanner();
    private final REPL repl = new REPL();

    @Inject
    public Launcher(XcbConnector connector, ModalRegistry modalRegistry, AnimatedWallpaperManager animatedWallpaper, ApplicationRunner applicationRunner, FontCache fontCache, HerbstClient herbstClient) {
        this.connector = connector;
        this.modalRegistry = modalRegistry;
        this.animatedWallpaper = animatedWallpaper;
        this.applicationRunner = applicationRunner;
        this.fontCache = fontCache;
        this.herbstClient = herbstClient;
    }

    public void bind() {
        herbstClient.addKeyHandler("Mod4-plus", () -> {
            if (!modalRegistry.closeCurrent()) {
                open();
            }
        });
    }

    private void open() {
        if (!pathScanner.isScanned()) {
            rescan();
        }

        TacUI ui = new TacUI(
                fontCache,
                connector,
                herbstClient.getCurrentMonitor()
        );
        ui.addFeature(new UseFeature());
        ui.addFeature(new CycleFeature());
        Instance instance = new Instance(ui);
        ui.addFeature(instance.textFieldFeature);
        modalRegistry.onOpen(ui);
        instance.refresh();
    }

    private class Instance {
        final TacUI ui;
        final Map<EntryDescriptor, LauncherEntry> entries;
        final TextFieldFeature textFieldFeature;
        final List<LauncherEntry> customEntries;

        /**
         * Python REPL
         */
        boolean replMode = false;
        String replResult;

        /**
         * Search in commands instead of looking for start only
         */
        boolean searchMode = false;

        public Instance(TacUI ui) {
            this.ui = ui;
            this.entries = new HashMap<>();
            this.textFieldFeature = new TextFieldFeature() {
                private static final String REPL_PREFIX = "#";

                @Override
                protected void onUpdate() {
                    replMode = getText().startsWith(REPL_PREFIX);
                    if (replMode) {
                        replResult = repl.run(getText().substring(REPL_PREFIX.length()));
                    } else {
                        searchMode = getText().contains("~");
                    }
                    refresh();
                }

                @Override
                protected String format(String text) {
                    if (replMode) {
                        return "# " + text.substring(REPL_PREFIX.length());
                    } else {
                        return (searchMode ? "~ " : "> ") + text;
                    }
                }
            };
            customEntries = Arrays.asList(
                    new LauncherEntry(this.ui, new EntryDescriptor(
                            "shutdown", new Command(LauncherConfig.INSTANCE.getShutdownCommand()), true), applicationRunner) {
                        @Override
                        public void onUsed() {
                            ui.close();
                            try {
                                animatedWallpaper.stop().get();
                                // wait for animation to finish
                            } catch (Exception ignored) {}
                            super.onUsed();

                            System.exit(0);
                        }
                    },

                    new LauncherEntry(this.ui, new EntryDescriptor("rehash", null, false), applicationRunner) {
                        @Override
                        public void onUsed() {
                            ui.close();
                            log.info("Requested rehash");
                            rescan();
                        }
                    }
            );
        }

        private void refresh() {
            if (replMode) {
                ui.setEntries(Arrays.stream(replResult.split("\n"))
                                      .map(ReplLineEntry::new));
                return;
            }

            Stream<EntryDescriptor> shortcuts = LauncherConfig.INSTANCE.getShortcuts().entrySet().stream()
                    .map(e -> new EntryDescriptor(e.getKey(), e.getValue(), true));
            Stream<EntryDescriptor> normal = pathScanner.getApplications().stream()
                    .map(s -> new EntryDescriptor(s, new Command(Collections.singletonList(s)), false));

            Stream<LauncherEntry> entryStream = Stream.concat(
                    Stream.concat(shortcuts.map(this::getLauncherEntry),
                                  customEntries.stream()),
                    normal.map(this::getLauncherEntry)
            );

            String filter;
            if (searchMode) {
                filter = textFieldFeature.getText().replace("~", "");
            } else {
                filter = textFieldFeature.getText();
            }
            ui.setEntries(entryStream.filter(e -> {
                if (searchMode) {
                    return Util.INSTANCE.containsIgnoreCaseAscii(e.getDescriptor().getTitle(), filter);
                } else {
                    return Util.INSTANCE.startsWithIgnoreCaseAscii(e.getDescriptor().getTitle(), filter);
                }
            }));
        }

        private LauncherEntry getLauncherEntry(EntryDescriptor d) {
            return entries.computeIfAbsent(d, t -> new LauncherEntry(ui, t, applicationRunner));
        }
    }

    private void rescan() {
        try {
            pathScanner.scan();
        } catch (UncheckedIOException e) {
            log.error("Error in path scanner", e);
        }
    }
}
