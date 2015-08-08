package at.yawk.wm.tac.password;

import at.yawk.password.MultiFileLocalStorageProvider;
import at.yawk.wm.Config;
import at.yawk.wm.Scheduler;
import at.yawk.wm.Util;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.*;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class PasswordManager {
    @Inject Scheduler scheduler;
    @Inject ModalRegistry modalRegistry;
    @Inject Config config;
    @Inject XcbConnector connector;

    PasswordHolder holder;

    @Inject
    void configure(ObjectMapper objectMapper) {
        PasswordConfig passwordConfig = config.getPassword();
        if (!Files.isDirectory(passwordConfig.getCacheDir())) {
            try {
                Files.createDirectories(passwordConfig.getCacheDir());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        holder = new PasswordHolder(
                new MultiFileLocalStorageProvider(passwordConfig.getCacheDir()),
                scheduler, objectMapper,
                new InetSocketAddress(passwordConfig.getHost(), passwordConfig.getPort()),
                passwordConfig.getTimeout()
        );
    }

    @Inject
    void bind(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-minus", () -> {
            if (!modalRegistry.closeCurrent()) {
                open();
            }
        });
    }

    private void open() {
        TacUI ui = new TacUI(
                config,
                connector,
                connector.getScreen().getWidth() - config.getTac().getWidth(),
                config.getDock().getHeight()
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

        boolean passwordPrompt;
        boolean loadRunning = true;
        String errorMessage = null;

        Action action = Action.COPY;
        String search = "";

        public Instance(TacUI ui) {
            if (holder.claim()) {
                passwordPrompt = false;
            } else {
                passwordPrompt = true;
            }

            ui.addFeature(new UseFeature() {
                @Override
                protected void onEnter() {
                    if (passwordPrompt) {
                        loadRunning = true;
                        refresh();
                        scheduler.execute(() -> {
                            try {
                                holder.claim(textFieldFeature.getText());
                                passwordPrompt = false;
                                textFieldFeature.clear();
                            } catch (Exception e) {
                                log.info("Failed to load password database", e);
                                errorMessage = e.getMessage();
                            }
                            refresh();
                        });
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
                    if (!passwordPrompt) {
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
                    if (passwordPrompt) {
                        StringBuilder builder = new StringBuilder("Password: ");
                        for (int i = 0; i < text.length(); i++) {
                            builder.append('\u00b7');
                        }
                        return builder.toString();
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
        }

        void refresh() {
            if (passwordPrompt) {
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
                        .filter(name -> Util.containsIgnoreCaseAscii(name, search))
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
