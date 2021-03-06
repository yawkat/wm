package at.yawk.wm.tac.password;

import at.yawk.password.MultiFileLocalStorageProvider;
import at.yawk.wm.Scheduler;
import at.yawk.wm.Util;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.*;
import at.yawk.wm.x.XcbConnector;
import at.yawk.wm.x.font.FontCache;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Singleton
public class PasswordManager {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(PasswordManager.class);
    private final Scheduler scheduler;
    private final ModalRegistry modalRegistry;
    private final XcbConnector connector;
    private final FontCache fontCache;
    private final HerbstClient herbstClient;

    PasswordHolder holder;

    @Inject
    public PasswordManager(Scheduler scheduler, ModalRegistry modalRegistry, XcbConnector connector, FontCache fontCache, HerbstClient herbstClient) {
        this.scheduler = scheduler;
        this.modalRegistry = modalRegistry;
        this.connector = connector;
        this.fontCache = fontCache;
        this.herbstClient = herbstClient;
    }

    public void configure() {
        Util.requireRuntime();
        if (!Files.isDirectory(PasswordConfig.INSTANCE.getCacheDir())) {
            try {
                Files.createDirectories(PasswordConfig.INSTANCE.getCacheDir());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        holder = new PasswordHolder(
                new MultiFileLocalStorageProvider(PasswordConfig.INSTANCE.getCacheDir().toFile()),
                scheduler,
                PasswordConfig.INSTANCE.getRemote(),
                PasswordConfig.INSTANCE.getTimeout()
        );
    }

    public void bind() {
        herbstClient.addKeyHandler("Mod4-minus", () -> {
            if (!modalRegistry.closeCurrent()) {
                open();
            }
        });
    }

    private void open() {
        TacUI ui = new TacUI(
                fontCache,
                connector,
                herbstClient.getCurrentMonitor()
        );
        ui.addFeature(new CycleFeature());
        Instance instance = new Instance(ui);
        modalRegistry.onOpen(ui);
        instance.refresh();
    }

    class Instance {
        final TacUI ui;
        /**
         * Title -> entry
         */
        final Map<String, PasswordEntry> entries = new HashMap<>();
        final TextFieldFeature textFieldFeature;

        @Nullable PasswordHolder.HolderClaim claim;

        boolean loadRunning = false;
        String errorMessage = null;

        Action action = Action.COPY;
        String search = "";

        public Instance(TacUI ui) {
            //noinspection RedundantIfStatement
            claim = holder.claim();

            ui.addFeature(new UseFeature() {
                @Override
                protected void onEnter() {
                    if (claim == null) {
                        if (!loadRunning) {
                            loadRunning = true;
                            refresh();
                            scheduler.execute(() -> {
                                try {
                                    claim = holder.claim(textFieldFeature.getText());
                                    textFieldFeature.clear();
                                } catch (Exception e) {
                                    log.info("Failed to load password database", e);
                                    errorMessage = e.getMessage();
                                }
                                loadRunning = false;
                                refresh();
                            });
                        }
                    } else if (action == Action.ADD) {
                        at.yawk.password.model.PasswordEntry e = new at.yawk.password.model.PasswordEntry();
                        e.setName(search);
                        e.setValue("");
                        new PasswordEditorWindow(PasswordManager.this, e, true).show();
                    } else {
                        super.onEnter();
                    }
                }
            });

            this.ui = ui;
            this.textFieldFeature = new TextFieldFeature() {
                @Override
                protected void onUpdate() {
                    if (claim != null) {
                        search = getText();
                        if (getText().indexOf('+') != -1) {
                            action = Action.ADD;
                            search = search.replaceFirst("\\+", "");
                        } else if (getText().indexOf('~') != -1) {
                            action = Action.VIEW;
                            search = search.replaceFirst("~", "");
                        } else {
                            action = Action.COPY;
                        }
                    }
                    refresh();
                }

                @Override
                protected String format(String text) {
                    if (claim == null) {
                        if (loadRunning) {
                            return "Loading...";
                        } else {
                            StringBuilder builder = new StringBuilder("Password: ");
                            for (int i = 0; i < text.length(); i++) {
                                builder.append('·');
                            }
                            return builder.toString();
                        }
                    }

                    switch (action) {
                    case ADD:
                        return "+ " + text;
                    case VIEW:
                        return "~ " + text;
                    default:
                        return (holder.isFromLocalStorage() ? "! " : "> ") + text;
                    }
                }
            };
            ui.addFeature(textFieldFeature);

            ui.addCloseListener(() -> {
                if (claim != null) {
                    claim.unclaim();
                }
            });
        }

        void refresh() {
            if (claim == null) {
                if (loadRunning) {
                }

                if (errorMessage == null) {
                    ui.setEntries(Stream.empty());
                } else {
                    Entry entry = new Entry() {};
                    entry.setSelectable(false);
                    entry.setState(new EntryState(errorMessage, false, false));
                    ui.setEntries(Stream.of(entry));
                }
                return;
            }
            if (action != Action.ADD) {
                Stream<PasswordEntry> entries = holder.getPasswords().getPasswords()
                        .stream()
                        .map(at.yawk.password.model.PasswordEntry::getName)
                        .filter(name -> Util.INSTANCE.containsIgnoreCaseAscii(name, search))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .map(name -> this.entries.computeIfAbsent(name, n -> new PasswordEntry(this, n)));
                ui.setEntries(entries);
            } else {
                ui.setEntries(Stream.empty());
            }
        }

        PasswordManager getManager() {
            return PasswordManager.this;
        }
    }

    enum Action {
        COPY,
        ADD,
        VIEW,
    }
}
