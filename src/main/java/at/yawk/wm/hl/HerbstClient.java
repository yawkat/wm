package at.yawk.wm.hl;

import at.yawk.wm.Util;
import at.yawk.yarn.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.DatatypeConverter;
import lombok.SneakyThrows;

/**
 * @author yawkat
 */
@Component
public class HerbstClient {
    @Inject Provider<HerbstEventBus> eventBus;

    private Map<String, Runnable> keyHandlers = new HashMap<>();

    private Process openProcess(String... action) throws IOException {
        List<String> command = new ArrayList<>(action.length + 1);
        command.add("herbstclient");
        command.addAll(Arrays.asList(action));
        return new ProcessBuilder(command).start();
    }

    @SneakyThrows
    private void send(String... action) {
        openProcess(action);
    }

    private InputStream stream(String... action) throws IOException {
        Process process = openProcess(action);
        return process.getInputStream();
    }

    @SneakyThrows
    private String dispatch(String... action) {
        InputStream in = stream(action);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] bytes = new byte[256];
        int len;
        while ((len = in.read(bytes)) != -1) {
            buf.write(bytes, 0, len);
        }
        return buf.toString("UTF-8");
    }

    @PostConstruct
    void listen() {
        Thread thread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    stream("--idle"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    List<String> components = Util.split(line, '\t', 4);
                    String verb = components.get(0);
                    switch (verb) {
                    case "tag_changed":
                        eventBus.get().post(new TagEvent());
                        break;
                    case "window_title_changed":
                    case "focus_changed":
                        if (components.size() > 2) {
                            eventBus.get().post(new TitleEvent(components.get(2)));
                        }
                        break;
                    case "reload":
                    case "quit_panel":
                        eventBus.get().post(new ShutdownEvent());
                        break;
                    case "_key_handler":
                        Runnable handler = keyHandlers.get(components.get(1));
                        if (handler != null) {
                            handler.run();
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public List<Tag> getTags() {
        return Util.split(
                dispatch("tag_status"),
                '\t',
                Integer.MAX_VALUE
        ).stream().map(t -> {
            Tag tag = new Tag();
            tag.setId(t.substring(1));
            switch (t.charAt(0)) {
            case '#':
                tag.setState(Tag.State.SELECTED);
                break;
            case ':':
            default:
                tag.setState(Tag.State.RUNNING);
                break;
            case '.':
                tag.setState(Tag.State.EMPTY);
                break;
            }
            return tag;
        }).collect(Collectors.toList());
    }

    public void advanceTag(int tagsToAdvance) {
        send("use_index", (tagsToAdvance < 0 ? "-" : "+") + Math.abs(tagsToAdvance));
    }

    public void pad(int pixels) {
        // todo: other monitors
        send("pad", "0", String.valueOf(pixels));
    }

    public void addKeyHandler(String key, Runnable task) {
        byte[] rb = new byte[32];
        ThreadLocalRandom.current().nextBytes(rb);
        String token = DatatypeConverter.printHexBinary(rb).toLowerCase();
        keyHandlers.put(token, task);
        send("keybind", key, "emit_hook", "_key_handler", token);
    }
}
