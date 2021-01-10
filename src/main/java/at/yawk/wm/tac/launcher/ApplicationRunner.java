package at.yawk.wm.tac.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yawkat
 */
@Singleton
public class ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ApplicationRunner.class);

    private final Path runWorkDir = Files.createTempDirectory("wm-run");
    private final AtomicInteger processCounter = new AtomicInteger(0);

    public ApplicationRunner() throws IOException {
    }

    public void run(Command command) {
        try {
            int id = processCounter.incrementAndGet();
            log.info("[{}] Executing {}", id, command);

            List<String> line;
            if (command.getJailOptions() != null) {
                line = new ArrayList<>();
                line.add("firejail");
                line.addAll(command.getJailOptions());
                line.add("--");
                line.addAll(command.getCommand());
            } else {
                line = command.getCommand();
            }

            log.debug("[{}] Final command line: {}", id, line);

            Process process = new ProcessBuilder()
                    .directory(runWorkDir.toFile())
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
