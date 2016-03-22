package at.yawk.wm.tac.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;
import org.slf4j.Logger;

/**
 * @author yawkat
 */
@Singleton
public class ApplicationRunner {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ApplicationRunner.class);
    private final AtomicInteger processCounter = new AtomicInteger(0);

    public void run(Command command) {
        try {
            int id = processCounter.incrementAndGet();
            log.info("[{}] Executing {}", id, command);

            List<String> line;
            if (command.isJail()) {
                line = new ArrayList<>();
                line.add("firejail");
                line.add("--");
                Collections.addAll(line, command.getCommand());
            } else {
                line = Arrays.asList(command.getCommand());
            }

            log.debug("[{}] Final command line: {}", id, line);

            Process process = new ProcessBuilder()
                    .command(line)
                    .redirectError(ProcessBuilder.Redirect.PIPE)
                    .redirectOutput(ProcessBuilder.Redirect.PIPE)
                    .start();

            logStream(id, "out", process.getInputStream());
            logStream(id, "err", process.getErrorStream());
        } catch (IOException e) {
            log.warn("Failed to run command {}", command, e);
        }
    }

    private void logStream(int processId, String streamName, InputStream stream) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("[{}:{}] {}", processId, streamName, line);
                }
            } catch (IOException e) {
                log.warn("Failed to log [{}:{}]", processId, streamName);
            }
        });
        thread.setName("Process Logger [" + processId + ":" + streamName + "]");
        thread.setDaemon(true);
        thread.start();
    }
}
