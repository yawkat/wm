package at.yawk.wm.tac.launcher;

import at.yawk.wm.Config;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.CycleFeature;
import at.yawk.wm.tac.ModalRegistry;
import at.yawk.wm.tac.TacUI;
import at.yawk.wm.tac.TextFieldFeature;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import java.io.IOException;
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

    @Inject
    public void bind(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-plus", this::open);
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
                @Override
                protected void onUpdate() {
                    refresh();
                }

                @Override
                protected String format(String text) {
                    return "> " + text;
                }
            };
        }

        private void refresh() {
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
                    e -> startsWithIgnoreCaseAscii(e.getDescriptor().getTitle(), filter)));
        }
    }

    private static boolean startsWithIgnoreCaseAscii(String s, String prefix) {
        if (prefix.length() > s.length()) { return false; }
        for (int i = 0; i < prefix.length(); i++) {
            char o = s.charAt(i);
            char p = prefix.charAt(i);
            if (o != p) {
                int ao = alphabetIndex(o);
                if (ao > 26 || ao != alphabetIndex(p)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Taken from guava, this returns 0 for a/A, 25 for z/Z and a larger value for any non-letter.
     */
    private static int alphabetIndex(char c) {
        return (char) ((c | 0x20) - 'a');
    }

    private void rescan() {
        try {
            pathScanner.scan();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
