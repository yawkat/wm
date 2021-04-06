package at.yawk.wm.hl;

import at.yawk.wm.Util;
import io.netty.util.internal.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Singleton
public class HerbstClient {
    private final HerbstEventBus eventBus;

    private Map<String, Runnable> keyHandlers = new HashMap<>();

    private Map<Integer, Monitor> monitors = null;
    private Monitor currentMonitor;

    @Inject
    public HerbstClient(HerbstEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public Monitor getCurrentMonitor() {
        if (currentMonitor == null) {
            if (monitors == null) { listMonitors(); }
            return monitors.values().iterator().next();
        }
        return currentMonitor;
    }

    private Process openProcess(String... action) throws IOException {
        List<String> command = new ArrayList<>(action.length + 1);
        command.add("herbstclient");
        command.addAll(Arrays.asList(action));
        return new ProcessBuilder(command).start();
    }

    @SneakyThrows
    private Process send(String... action) {
        return openProcess(action);
    }

    private InputStream stream(String... action) throws IOException {
        Process process = openProcess(action);
        return process.getInputStream();
    }

    @SneakyThrows
    private String dispatch(String... action) {
        try (InputStream in = stream(action)) {
            return Util.INSTANCE.streamToString(in, 256);
        }
    }

    public void listen() {
        Thread thread = new Thread(() -> {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    stream("--idle"), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine()) != null) {
                    List<String> components = Util.INSTANCE.split(line, '\t', 4);
                    String verb = components.get(0);
                    switch (verb) {
                    case "tag_changed":
                        currentMonitor = monitors.get(Integer.parseInt(components.get(2)));
                    case "tag_flags":
                        eventBus.post(new TagEvent());
                        break;
                    case "window_title_changed":
                    case "focus_changed":
                        if (components.size() > 2) {
                            eventBus.post(new TitleEvent(components.get(2)));
                        }
                        break;
                    case "reload":
                    case "quit_panel":
                        eventBus.post(new ShutdownEvent());
                        break;
                    case "_key_handler":
                        Runnable handler = keyHandlers.get(components.get(1));
                        if (handler != null) {
                            try {
                                handler.run();
                            } catch (Throwable t) {
                                log.error("Failed to pass key event", t);
                            }
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

    public List<Tag> getTags(Monitor monitor) {
        return Util.INSTANCE.split(
                dispatch("tag_status", String.valueOf(monitor.getId())),
                '\t',
                Integer.MAX_VALUE
        ).stream().map(t -> {
            Tag tag = new Tag();
            tag.setId(t.substring(1));
            switch (t.charAt(0)) {
            case '#':
                currentMonitor = monitor;
            case '+':
                tag.setState(Tag.State.SELECTED);
                break;
            case '%':
            case '-':
                tag.setState(Tag.State.SELECTED_ELSEWHERE);
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

    @SneakyThrows
    public void focusMonitor(Monitor monitor) {
        if (getCurrentMonitor().getId() != monitor.getId()) {
            send("focus_monitor", String.valueOf(monitor.getId())).waitFor(1, TimeUnit.SECONDS);
        }
        currentMonitor = monitor;
    }

    public void advanceTag(int tagsToAdvance) {
        send("use_index", (tagsToAdvance < 0 ? "-" : "+") + Math.abs(tagsToAdvance), "--skip-visible");
    }

    public void pad(Monitor monitor, int pixels) {
        // todo: other monitors
        send("pad", String.valueOf(monitor.getId()), String.valueOf(pixels));
    }

    public void addKeyHandler(String key, Runnable task) {
        byte[] rb = new byte[32];
        ThreadLocalRandom.current().nextBytes(rb);
        String token = StringUtil.toHexString(rb).toLowerCase();
        keyHandlers.put(token, task);
        send("keybind", key, "emit_hook", "_key_handler", token);
    }

    private static final Pattern MONITOR_PATTERN = Pattern.compile(
            "(\\d+): (\\d+)x(\\d+)\\+(\\d+)\\+(\\d+) with tag \".*\"(.*)");

    public List<Monitor> listMonitors() {
        String[] monitorLines = dispatch("list_monitors").split("\n");
        List<Monitor> monitors = Stream.of(monitorLines)
                .map(line -> {
                    Matcher matcher = MONITOR_PATTERN.matcher(line);
                    if (!matcher.matches()) { throw new IllegalStateException(); }
                    return new Monitor(
                            Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(4)),
                            Integer.parseInt(matcher.group(5)),
                            Integer.parseInt(matcher.group(2)),
                            Integer.parseInt(matcher.group(3)),
                            !matcher.group(6).isEmpty()
                    );
                })
                .collect(Collectors.toList());
        this.monitors = monitors.stream().collect(Collectors.toMap(Monitor::getId, m -> m));
        return monitors;
    }
}
