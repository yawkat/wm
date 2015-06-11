package at.yawk.wm.tac.launcher;

import at.yawk.wm.Config;
import at.yawk.wm.Util;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.*;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.inject.Inject;

/**
 * @author yawkat
 */
@Component
public class Launcher {

    @Inject Config config;
    @Inject XcbConnector connector;
    @Inject ModalRegistry modalRegistry;

    private final PathScanner pathScanner = new PathScanner();
    private final REPL repl = new REPL();

    @Inject
    public void bind(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-plus", () -> {
            if (!modalRegistry.closeCurrent()) {
                open();
            }
        });
    }

    public void open() {
        if (!pathScanner.isScanned()) {
            rescan();
        }

        TacUI ui = new TacUI(
                config,
                connector,
                connector.getScreen().getWidth() - config.getTac().getWidth(),
                config.getDock().getHeight()
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
        final LauncherEntry rehash;

        boolean replMode = false;
        String replResult;

        public Instance(TacUI ui) {
            this.ui = ui;
            this.entries = new HashMap<>();
            this.rehash = new LauncherEntry(this.ui, new EntryDescriptor("rehash", null, false)) {
                @Override
                public void onUsed() {
                    ui.close();
                    try {
                        pathScanner.scan();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            this.textFieldFeature = new TextFieldFeature() {
                private static final String REPL_PREFIX = "#";

                @Override
                protected void onUpdate() {
                    replMode = getText().startsWith(REPL_PREFIX);
                    if (replMode) {
                        replResult = repl.run(getText().substring(REPL_PREFIX.length()));
                    }
                    refresh();
                }

                @Override
                protected String format(String text) {
                    if (replMode) {
                        return "# " + text.substring(REPL_PREFIX.length());
                    } else {
                        return "> " + text;
                    }
                }
            };
        }

        private void refresh() {
            if (replMode) {
                ui.setEntries(Arrays.stream(replResult.split("\n"))
                                      .map(ReplLineEntry::new));
                return;
            }

            Stream<EntryDescriptor> shortcuts = config.getShortcuts().entrySet().stream()
                    .map(e -> new EntryDescriptor(e.getKey(), e.getValue(), true));
            Stream<EntryDescriptor> normal = pathScanner.getApplications().stream()
                    .map(s -> new EntryDescriptor(s, s, false));
            Stream<LauncherEntry> rehash = Stream.of(this.rehash);

            Stream<LauncherEntry> entryStream = Stream.concat(
                    Stream.concat(shortcuts, normal).map(
                            d -> entries.computeIfAbsent(d, t -> new LauncherEntry(ui, t))),
                    rehash
            );

            String filter = textFieldFeature.getText();
            ui.setEntries(entryStream.filter(
                    e -> Util.startsWithIgnoreCaseAscii(e.getDescriptor().getTitle(), filter)));
        }
    }

    private void rescan() {
        try {
            pathScanner.scan();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
