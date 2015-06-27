package at.yawk.wm.paste;

import at.yawk.paste.client.ClipboardHelper;
import at.yawk.paste.client.PasteClient;
import at.yawk.paste.model.PasteData;
import at.yawk.wm.Config;
import at.yawk.wm.hl.HerbstClient;
import at.yawk.wm.tac.ModalRegistry;
import at.yawk.wm.x.XcbConnector;
import at.yawk.yarn.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yawkat
 */
@Component
@Slf4j
public class PasteManager {
    private PasteClient client;
    private ClipboardHelper clipboardHelper;

    @Inject XcbConnector connector;
    @Inject ModalRegistry modalRegistry;

    @Inject
    void load(Config config, ObjectMapper objectMapper) {
        at.yawk.paste.client.Config pasteConfig = config.getPaste();
        if (pasteConfig == null) { pasteConfig = new at.yawk.paste.client.Config(); }
        client = new PasteClient(pasteConfig, objectMapper);
        clipboardHelper = new ClipboardHelper(pasteConfig);
    }

    @Inject
    void setupKeys(HerbstClient herbstClient) {
        herbstClient.addKeyHandler("Mod4-v", this::makeScreenshot);
    }

    void pasteFromClipboard() {
        try {
            PasteData data = clipboardHelper.getCurrentClipboardData();
            if (data != null) {
                upload(data);
            } else {
                sendNotification("Clipboard contents not recognized");
            }
        } catch (IOException e) {
            log.error("Failed to load paste from clipboard", e);
            sendNotification("Error: " + e.getMessage());
        }
    }

    void makeScreenshot() {
        ScreenshotOverlay overlay = new ScreenshotOverlay(this, connector);
        overlay.capture();
        modalRegistry.onOpen(overlay);
        overlay.open();
    }

    private void upload(PasteData data) {
        try {
            String url = client.save(data);
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(url), null);
            sendNotification("Paste uploaded: " + url);
        } catch (IOException e) {
            log.error("Failed to save paste", e);
            sendNotification("Error: " + e.getMessage());
        }
    }

    private void sendNotification(String message) {
        log.debug("Sending notification '{}'", message);
        try {
            new ProcessBuilder("notify-send", "--expire-time=2000", message)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                    .start();
        } catch (IOException e) {
            log.error("Failed to send upload notification", e);
        }
    }
}
